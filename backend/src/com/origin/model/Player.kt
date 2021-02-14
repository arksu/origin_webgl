package com.origin.model

import com.origin.Const
import com.origin.collision.CollisionResult
import com.origin.entity.Character
import com.origin.entity.EntityObject
import com.origin.model.BroadcastEvent.ChatMessage.Companion.SYSTEM
import com.origin.model.move.Move2Object
import com.origin.model.move.Move2Point
import com.origin.model.move.MoveMode
import com.origin.model.move.Position
import com.origin.net.model.*
import com.origin.utils.ObjectID
import com.origin.utils.Rect
import com.origin.utils.TILE_SIZE
import com.origin.utils.Vec2i
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@ObsoleteCoroutinesApi
class PlayerMsg {
    class Connected
    class Disconnected
    class Store
    class MapClick(val btn: ClientButton, val flags: Int, val x: Int, val y: Int)
    class ObjectClick(val id: ObjectID, val flags: Int, val x: Int, val y: Int)
    class ObjectRightClick(val id: ObjectID)
    class ContextMenuItem(val item: String)
    class ExecuteActionCondition(val resp: CompletableDeferred<Boolean>, val block: (Player) -> Boolean)
}

private val PLAYER_RECT = Rect(3)

/**
 * инстанс персонажа игрока в игровом мире (игрок)
 */
@ObsoleteCoroutinesApi
class Player(
    /**
     * персонаж игрока (сущность хранимая в БД)
     */
    private val character: Character,

    val session: GameSession,
) : Human(character.id.value, character.x, character.y, character.level, character.region, character.heading) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Player::class.java)
    }

    private var moveMode = MoveMode.WALK

    /**
     * команда для отложенного выполнения (клик по карте)
     */
    private var commandToExecute: String? = null

    /**
     * одежда (во что одет игрок)
     */
//    private val paperdoll: Paperdoll = Paperdoll(this)

    override val status = PcStatus(this, character)

    /**
     * контекстное меню активное в данный момент
     */
    private var contextMenu: ContextMenu? = null

    /**
     * внешний вид персонажа (имя, пол, волосы и тд)
     */
    val appearance: PcAppearance = PcAppearance(character.name, "", 0)

    /**
     * при создании (логине) игрока запомним какой у него был онлайн
     */
    private var lastOnlineStore = System.currentTimeMillis()

    /**
     * корутина на периодическое сохранение состояния в базу
     */
    private var autoSaveJob: Job? = null

    override suspend fun processMessage(msg: Any) {
        logger.debug("Player $this msg ${msg.javaClass.simpleName}")
        when (msg) {
            is PlayerMsg.Connected -> connected()
            is PlayerMsg.Disconnected -> disconnected()
            is PlayerMsg.Store -> store()
            is PlayerMsg.MapClick -> mapClick(msg.btn, msg.flags, msg.x, msg.y)
            is PlayerMsg.ObjectClick -> objectClick(msg.id, msg.flags, msg.x, msg.y)
            is PlayerMsg.ObjectRightClick -> objectRightClick(msg.id)
            is PlayerMsg.ContextMenuItem -> contextMenuItem(msg.item)
            is BroadcastEvent.ChatMessage -> chatMessage(msg)
            is PlayerMsg.ExecuteActionCondition -> {
                val result = msg.block(this)
                if (!result) stopAction()
                msg.resp.complete(result)
            }

            else -> super.processMessage(msg)
        }
    }

    /**
     * клиент: клик по карте
     */
    private suspend fun mapClick(btn: ClientButton, flags: Int, x: Int, y: Int) {
        logger.debug("mapClick $x $y $btn")

        if (contextMenu != null) {
            session.send(ContextMenu(null))
            contextMenu = null
        }

        if (btn == ClientButton.LEFT) {
            if (commandToExecute != null) {
                if (flags and SHIFT_KEY > 0) {
                    logger.warn("SHIFT")
                    val xx = x / TILE_SIZE * TILE_SIZE + TILE_SIZE / 2
                    val yy = y / TILE_SIZE * TILE_SIZE + TILE_SIZE / 2
                    executeCommand(xx, yy)
                } else
                    executeCommand(x, y)
                commandToExecute = null
            } else {
                startMove(Move2Point(this, x, y))
            }
        }
    }

    /**
     * клик по объекту в мире
     */
    private suspend fun objectClick(id: ObjectID, flags: Int, x: Int, y: Int) {
        logger.debug("objectClick $id")
        if (contextMenu != null) {
            session.send(ContextMenu(null))
            contextMenu = null
        }
        val obj = knownList.getKnownObject(id)
        if (obj != null) {
            // если дистанция между объектом и местом клика меньше порога - считаем что попали в объект
            if (obj.pos.point.dist(Vec2i(x, y)) < 12) {
                // пока просто движемся к объекту
                goAndOpenObject(obj)
            } else {
                startMove(Move2Point(this, x, y))
            }
        }
    }

    private suspend fun goAndOpenObject(obj: GameObject) {
        // проверим расстояние от меня до объекта
        val myRect = getBoundRect().clone().move(pos.point)
        val objRect = obj.getBoundRect().clone().move(obj.pos.point)
        val (mx, my) = myRect.min(objRect)
        logger.debug("goAndOpenObject min $mx $my")
        if (mx <= OPEN_DISTANCE && my <= OPEN_DISTANCE) {
            openObjectsList.open(obj)
        } else {
            startMove(Move2Object(this, obj) {
                openObjectsList.open(obj)
            })
        }
    }

    /**
     * правый клик по объекту
     */
    private suspend fun objectRightClick(id: ObjectID) {
        logger.debug("objectRightClick $id")
        // если уже есть активное контекстное меню на экране
        if (contextMenu != null) {
            // пошлем отменю КМ
            session.send(ContextMenu(null))
            contextMenu = null
        } else {
            // попробуем вызывать КМ у объекта
            val obj = knownList.getKnownObject(id)
            contextMenu = obj?.contextMenu(this)
            if (contextMenu != null) {
                session.send(ContextMenu(contextMenu!!))
            } else {
                if (obj != null) {
                    goAndOpenObject(obj)
                }
            }
        }
    }

    /**
     * вырбан пункт контекстного меню
     */
    private suspend fun contextMenuItem(item: String) {
        contextMenu?.processItem(this, item)
        contextMenu = null
    }

    private suspend fun chatMessage(msg: BroadcastEvent.ChatMessage) {
        if (msg.channel == 0) {
            if (knownList.isKnownObject(msg.obj)) {
                session.send(CreatureSay(msg))
            }
        }
    }

    override fun getMovementMode(): MoveMode {
        return moveMode
    }

    override fun getBoundRect(): Rect {
        return PLAYER_RECT
    }

    override fun getResourcePath(): String {
        return "player"
    }

    /**
     * вызывается в самую последнюю очередь при спавне игрока в мир
     * когда уже все прогружено и заспавнено, гриды активированы
     */
    private fun connected() {
        World.addPlayer(this)

        // auto save task
        autoSaveJob = WorkerScope.launch {
            while (true) {
                delay(10000L)
                this@Player.send(PlayerMsg.Store())
            }
        }
    }

    /**
     * игровой клиент (аккаунт) отключился от игрока
     */
    private suspend fun disconnected() {
        autoSaveJob?.cancel()
        autoSaveJob = null

        World.removePlayer(this)

        if (spawned) {
            status.stopRegeneration()
            // удалить объект из грида
            remove()
        }
        store()
    }

    override suspend fun loadGrids() {
        super.loadGrids()
        session.send(MapGridConfirm())
    }

    override suspend fun onGridChanged() {
        super.onGridChanged()
        session.send(MapGridConfirm())
    }

    /**
     * сохранение текущей позиции в бд
     */
    override fun storePositionInDb() {
        logger.warn("storePositionInDb ${pos.x} ${pos.y}")
        WorkerScope.launch {
            transaction {
                character.x = pos.x
                character.y = pos.y
                character.level = pos.level
                character.region = pos.region
                character.heading = pos.heading

                character.flush()
            }
        }
    }

    /**
     * сохранение состояния игрока в базу
     */
    private fun store() {
        logger.debug("store player $this")
        val currentMillis = System.currentTimeMillis()

        transaction {
            character.onlineTime += TimeUnit.MILLISECONDS.toSeconds(currentMillis - lastOnlineStore)

            character.SHP = status.currentSoftHp
            character.HHP = status.currentHardHp
            character.stamina = status.currentStamina
            character.energy = status.currentEnergy
        }
        lastOnlineStore = currentMillis
    }

    /**
     * обработка команд в консоли
     */
    suspend fun consoleCommand(cmd: String) {
        logger.warn("adminCommand $cmd")
        val params = cmd.split(" ")
        when (params[0]) {
            "online" -> {
                session.send(CreatureSay(0, "online: ${World.getPlayersCount()}", SYSTEM))
            }
            "spawn" -> {
                commandToExecute = cmd
            }
            "tile" -> {
                commandToExecute = cmd
            }
        }
    }

    /**
     * выполнить ранее сохраненную команду в указанных координатах
     */
    private suspend fun executeCommand(x: Int, y: Int) {
        val params = commandToExecute!!.split(" ")
        when (params[0]) {
            "spawn" -> {
                // param 1 - type id
                val t: Int = params[1].toInt()
                // param 2 - data for object
                val d = if (params.size >= 3) params[2] else null
                val obj = transaction {
                    val e = EntityObject.makeNew(Position(x, y, pos), t)
                    if (d != null) {
                        e.data = d
                    }
                    Const.getObjectByType(e)
                }
                obj.pos.setGrid()
                val resp = CompletableDeferred<CollisionResult>()
                obj.grid.send(GridMsg.Spawn(obj, resp))
                resp.await()
            }
            "tile" -> {
                // param 1 - tile type
                val t = params[1].toByte()
                val p = Position(x, y, pos)
                World.getGrid(p).send(GridMsg.SetTile(p, t))
            }
            "tp" -> {
                // Teleport to
//                if (params.size == 3) {
//                    val x: Int = params[1].toInt()
//                    val y: Int = params[2].toInt()
                // TODO teleport cmd
//                }
            }
        }
    }

    override fun getMaxSoftHp(): Double {
        return status.currentHardHp
    }

    override fun getMaxStamina(): Double {
        return 100.0
    }

    private fun getMaxHp(): Double {
        // TODO stat CON
        return 100.0
    }

    private fun getMaxEnergy(): Double {
        return 10000.0
    }

    override fun broadcastStatusUpdate() {
        val su = StatusUpdate(this)
        su.addAttribute(CUR_SHP, status.currentSoftHp.roundToInt())
        su.addAttribute(CUR_HHP, status.currentHardHp.roundToInt())
        su.addAttribute(MAX_HP, getMaxHp().roundToInt())

        su.addAttribute(CUR_STAMINA, status.currentStamina.roundToInt())
        su.addAttribute(MAX_STAMINA, getMaxStamina().roundToInt())

        su.addAttribute(CUR_ENERGY, status.currentEnergy.roundToInt())
        su.addAttribute(MAX_ENERGY, getMaxEnergy().roundToInt())

        runBlocking(IO) {
            session.send(su)
        }

        // broadcast my status to party members
    }

    override fun toString(): String {
        return "${this::class.simpleName} ${appearance.visibleName} [$id] ${pos.point}"
    }
}