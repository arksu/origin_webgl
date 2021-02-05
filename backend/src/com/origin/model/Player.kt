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
import com.origin.net.model.CreatureSay
import com.origin.net.model.GameSession
import com.origin.net.model.MapGridConfirm
import com.origin.utils.ObjectID
import com.origin.utils.Rect
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

class PlayerMsg {
    class Connected
    class Disconnected
    class MapClick(val x: Int, val y: Int)
    class ObjectClick(val id: ObjectID, val x: Int, val y: Int)
    class ObjectRightClick(val id: ObjectID)
    class ContextMenuItem(val item: String)
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

    private var moveMode = MoveMode.WALK

    /**
     * команда для отложенного выполнения (клик по карте)
     */
    private var commandToExecute: String? = null

    /**
     * одежда (во что одет игрок)
     */
//    private val paperdoll: Paperdoll = Paperdoll(this)

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
    private val onlineBeginTime = System.currentTimeMillis()

    override suspend fun processMessage(msg: Any) {
        logger.debug("Player $this msg ${msg.javaClass.simpleName}")
        when (msg) {
            is PlayerMsg.Connected -> connected()
            is PlayerMsg.Disconnected -> disconnected()
            is PlayerMsg.MapClick -> mapClick(msg.x, msg.y)
            is PlayerMsg.ObjectClick -> objectClick(msg.id, msg.x, msg.y)
            is PlayerMsg.ObjectRightClick -> objectRightClick(msg.id)
            is PlayerMsg.ContextMenuItem -> contextMenuItem(msg.item)
            is BroadcastEvent.ChatMessage -> chatMessage(msg)

            else -> super.processMessage(msg)
        }
    }

    /**
     * клиент: клик по карте
     */
    private suspend fun mapClick(x: Int, y: Int) {
        logger.debug("mapClick $x $y")

        if (commandToExecute != null) {
            executeCommand(x, y)
            commandToExecute = null
        } else {
            startMove(Move2Point(this, x, y))
        }
    }

    /**
     * клик по объекту в мире
     */
    @Suppress("UNUSED_PARAMETER")
    private suspend fun objectClick(id: ObjectID, x: Int, y: Int) {
        logger.debug("objectClick $id")
        val obj = knownList.getKnownObject(id)
        if (obj != null) {
            // пока просто движемся к объекту
            startMove(Move2Object(this, obj))
        }
    }

    /**
     * правый клик по объекту
     */
    private suspend fun objectRightClick(id: ObjectID) {
        logger.debug("objectRightClick $id")
        contextMenu = knownList.getKnownObject(id)?.contextMenu(this)
        if (contextMenu != null) {
            session.send(com.origin.net.model.ContextMenu(contextMenu!!))
        }
    }

    /**
     * вырбан пункт контекстного меню
     */
    private suspend fun contextMenuItem(item: String) {
        contextMenu?.processItem(item)
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
    }

    /**
     * игровой клиент (аккаунт) отключился от игрока
     */
    private suspend fun disconnected() {
        World.removePlayer(this)

        // удалить объект из мира
        remove()

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
        var totalOnlineTime = character.onlineTime
        if (onlineBeginTime > 0) {
            totalOnlineTime += TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - onlineBeginTime)
        }
        transaction {
            character.onlineTime = totalOnlineTime
        }
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
                val resp = CompletableDeferred<CollisionResult>()
                grid.send(GridMsg.Spawn(obj, resp))
                resp.await()
            }
            "tile" -> {
                // param 1 - tile type
                val t = params[1].toByte()
                val p = Position(x, y, pos)
                World.getGrid(p).send(GridMsg.SetTile(p, t))
            }
        }
    }
}