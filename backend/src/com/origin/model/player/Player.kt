package com.origin.model

import com.origin.TimeController
import com.origin.database.dbQueryCoroutine
import com.origin.entity.Character
import com.origin.entity.EntityObject
import com.origin.entity.InventoryItemEntity
import com.origin.idfactory.IdFactory
import com.origin.model.BroadcastEvent.ChatMessage.Companion.SYSTEM
import com.origin.model.craft.Craft
import com.origin.model.inventory.Hand
import com.origin.model.inventory.Inventory
import com.origin.model.inventory.InventoryItem
import com.origin.model.inventory.ItemType
import com.origin.model.move.*
import com.origin.model.objects.ObjectsFactory
import com.origin.model.player.PcStatus
import com.origin.model.skills.SkillsList
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

@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
sealed class PlayerMsg {
    class Connected
    class Disconnected
    class Store
    class MapClick(val btn: ClientButton, val flags: Int, val x: Int, val y: Int)
    class ObjectClick(val id: ObjectID, val flags: Int, val x: Int, val y: Int)
    class ObjectRightClick(val id: ObjectID)
    class ContextMenuItem(val item: String)
    class ExecuteActionCondition(val resp: CompletableDeferred<Boolean>, val block: (Player) -> Boolean)
    class ItemClick(val id: ObjectID, val inventoryId: ObjectID, val x: Int, val y: Int, val ox: Int, val oy: Int)
    class InventoryClose(val inventoryId: ObjectID)
    class Craft(val name: String, val all: Boolean)
}

private val PLAYER_RECT = Rect(3)

/**
 * инстанс персонажа игрока в игровом мире (игрок)
 */
@DelicateCoroutinesApi
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
    private val paperdoll: Paperdoll = Paperdoll(this)

    /**
     * инвентарь игрока
     */
    override val inventory = Inventory(this)

    /**
     * вещь, которую держим в данный момент в руке
     */
    var hand: Hand? = null

    /**
     * скиллы которые изучил игрок
     */
    val skills = SkillsList(this)

    override val status = PcStatus(this, character)

    /**
     * контекстное меню активное в данный момент
     */
    private var contextMenu: ContextMenu? = null

    /**
     * внешний вид персонажа (имя, пол, волосы и тд)
     */
    val appearance = PcAppearance(character.name, "", 0)

    /**
     * при создании (логине) игрока запомним какой у него был онлайн
     */
    private var lastOnlineStoreTime = System.currentTimeMillis()

    /**
     * корутина на периодическое сохранение состояния в базу
     */
    private var autoSaveJob: Job? = null

    /**
     * таск на отправку текущего игрового времени клиенту
     */
    private var timeUpdateJob: Job? = null

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
            is PlayerMsg.ItemClick -> itemClick(msg)
            is PlayerMsg.InventoryClose -> {
                if (msg.inventoryId == id) {
                    session.send(InventoryClose(id))
                } else {
                    openObjectsList.close(msg.inventoryId)
                }
            }
            is GameObjectMsg.PutItem -> {
                // кто-то извне мне кладет вещь в инвентарь (генерация объектов)
                val success = putItem(msg.item)
                msg.resp.complete(success)
            }
            is PlayerMsg.Craft -> craft(msg)

            else -> super.processMessage(msg)
        }
    }

    private suspend fun craft(msg: PlayerMsg.Craft) {
        val craft = Craft.findByName(msg.name)
        if (craft != null) {
            // проверим что есть все необходимые ингридиенты в инвентаре
            if (inventory.findAndTakeItem(craft.required, false) == null) {
                saySystem("not enough items for craft ${craft.getHumanReadableName()}")
                return
            }

            // запускаем action, затем спавним результат
            startActionOnce(
                this,
                craft.ticksPerStep,
                craft.steps,
                Status.reduceStamina(craft.staminaCost)
            ) { _, _ ->
                // ищем и берем вещи из инвентаря. вернет список только если все требуемые вещи есть в полном объеме в инвентаре
                val result = inventory.findAndTakeItem(craft.required)

                // вернулся список - вещи нашлись
                if (result != null) {
                    // удалим их навсегда из игры, поскольку они используются для крафта вещи
                    result.forEach {
                        it.delete()
                    }
                    // создаем вещи по рецепту из крафта в нужном количестве
                    craft.produce.forEach {
                        var left = it.count
                        val type = it.item
                        while (left > 0) {
                            left--
                            // TODO: рассчитаем качество создаваемой вещи
                            val q: Short = 10
                            // создаем новую вещь из списка produced крафта
                            val newItem = transaction {
                                val e = InventoryItemEntity.makeNew(type, q)
                                InventoryItem(e, null)
                            }
                            // пытаемся положить вещь в наш инвентарь, если не удается - прекращаем спавн вещей
                            if (!putItem(newItem)) return@forEach
                        }
                    }
                } else {
                    saySystem("not enough items for craft ${craft.getHumanReadableName()}")
                }
                true
            }
        }
    }

    private suspend fun putItem(item: InventoryItem): Boolean {
        // пробуем положить вещь в инвентарь
        val success = inventory.putItem(item)
        if (!success) {
            // если не удалось пробуем ее заспавнить на землю
            dropItem(item)
        }
        return success
    }

    private suspend fun dropItem(item: InventoryItem): Boolean {
        // TODO new item drop to ground
        return true
    }

    private suspend fun itemClick(msg: PlayerMsg.ItemClick) {
        if (contextMenu != null) clearContextMenu()

        // держим в руке что-то?
        val h = hand
        if (h == null) {
            // в руке ничего нет. возьмем из инвентаря
            val taken = if (msg.inventoryId == id) {
                inventory.takeItem(msg.id)
            } else {
                val obj = openObjectsList.get(msg.inventoryId)
                if (obj != null) {
                    // возьмем из объекта вещь
                    val result = CompletableDeferred<InventoryItem?>()
                    obj.send(GameObjectMsg.TakeItem(this, msg.id, result))
                    result.await()
                } else null
            }
            // взяли вещь из инвентаря
            if (taken != null) {
                setHand(taken, msg)
            }
        } else {
            // в руке ЕСТЬ вещь
            // если попали в пустой слот
            if (msg.id == 0L) {
                // кликнули в мой инвентарь?
                val success = if (msg.inventoryId == id) {
                    inventory.putItem(h.item, msg.x - h.offsetX, msg.y - h.offsetY)
                } else {
                    val obj = openObjectsList.get(msg.inventoryId)
                    if (obj != null) {
                        val result = CompletableDeferred<Boolean>()
                        obj.send(GameObjectMsg.PutItem(h.item, msg.x - h.offsetX, msg.y - h.offsetY, result))
                        result.await()
                    } else false
                }
                if (success) {
                    setHand(null, msg)
                }
            }
        }
    }

    /**
     * клиент: клик по карте
     */
    private suspend fun mapClick(btn: ClientButton, flags: Int, x: Int, y: Int) {
        logger.debug("mapClick $x $y $btn")

        if (contextMenu != null) {
            clearContextMenu()
        }

        if (btn == ClientButton.LEFT) {
            // если что-то держим в руке надо дропнуть это
            if (hand != null) {
                dropHandItem()
            } else {
                if (commandToExecute != null) {
                    if (flags and SHIFT_KEY > 0) {
                        logger.warn("SHIFT")
                        val xx = x / TILE_SIZE * TILE_SIZE + TILE_SIZE / 2
                        val yy = y / TILE_SIZE * TILE_SIZE + TILE_SIZE / 2
                        if (executeCommand(xx, yy)) commandToExecute = null
                    } else if (executeCommand(x, y)) commandToExecute = null
                } else {
                    startMove(Move2Point(this, x, y))
                }
            }
        }
    }

    /**
     * клик по объекту в мире
     */
    private suspend fun objectClick(id: ObjectID, flags: Int, x: Int, y: Int) {
        logger.debug("objectClick $id")

        if (contextMenu != null) {
            clearContextMenu()
        }
        val obj = knownList.getKnownObject(id)
        if (obj != null) {
            // если дистанция между объектом и местом клика меньше порога - считаем что попали в объект
            if (obj.pos.point.dist(Vec2i(x, y)) < 12) {
                // пока просто движемся к объекту
                goAndOpenObject(obj)
            } else if (hand == null) {
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
            startMove(
                Move2Object(this, obj) {
                    openObjectsList.open(obj)
                }
            )
        }
    }

    /**
     * правый клик по объекту
     */
    private suspend fun objectRightClick(id: ObjectID) {
        logger.debug("objectRightClick $id")
        if (hand != null) return

        // если уже есть активное контекстное меню на экране
        if (contextMenu != null) {
            // пошлем отмену КМ
            clearContextMenu()
        } else {
            // попробуем вызывать КМ у объекта
            val obj = knownList.getKnownObject(id)
            contextMenu = obj?.contextMenu(this)
            if (contextMenu != null) {
                session.send(ContextMenuData(contextMenu!!))
            } else {
                if (obj != null) {
                    goAndOpenObject(obj)
                }
            }
        }
    }

    private suspend fun setHand(item: InventoryItem?, msg: PlayerMsg.ItemClick) {
        hand = if (item != null) {
            val h = Hand(this, item, msg.x, msg.y, msg.ox, msg.oy)
            session.send(HandUpdate(h))
            h
        } else {
            session.send(HandUpdate())
            null
        }
    }

    private suspend fun clearContextMenu() {
        session.send(ContextMenuData(null))
        contextMenu = null
    }

    private suspend fun dropHandItem() {
        // TODO dropHandItem
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
    private suspend fun connected() {
        World.addPlayer(this)

        // auto save task
        autoSaveJob = WorkerScope.launch {
            while (true) {
                delay(10000L)
                this@Player.send(PlayerMsg.Store())
            }
        }
        timeUpdateJob = WorkerScope.launch {
            while (true) {
                this@Player.session.send(
                    TimeUpdate(
                        TimeController.tickCount,
                        TimeController.getGameHour(),
                        TimeController.getGameMinute(),
                        TimeController.getGameDay(),
                        TimeController.getGameMonth(),
                        TimeController.getNightValue(),
                        TimeController.getSunValue(),
                        0
                    )
                )
                delay(3000L)
            }
        }

        session.send(CraftList(this))
    }

    /**
     * игровой клиент (аккаунт) отключился от игрока
     */
    private suspend fun disconnected() {
        autoSaveJob?.cancel()
        autoSaveJob = null
        timeUpdateJob?.cancel()
        timeUpdateJob = null

        World.removePlayer(this)

        if (spawned) {
            status.stopRegeneration()
            openObjectsList.closeAll()
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
        dbQueryCoroutine {
            character.x = pos.x
            character.y = pos.y
            character.level = pos.level
            character.region = pos.region
            character.heading = pos.heading

            character.flush()
        }
    }

    /**
     * сохранение состояния игрока в базу
     */
    private fun store() {
        logger.debug("store player $this")
        val currentMillis = System.currentTimeMillis()

        transaction {
            character.onlineTime += TimeUnit.MILLISECONDS.toSeconds(currentMillis - lastOnlineStoreTime)

            status.storeToCharacter(character)
        }
        lastOnlineStoreTime = currentMillis
    }

    private suspend fun saySystem(text: String) {
        session.send(CreatureSay(0, text, SYSTEM))
    }

    /**
     * обработка команд в консоли
     */
    suspend fun consoleCommand(cmd: String) {
        logger.warn("adminCommand $cmd")
        val params = cmd.split(" ")
        when (params[0]) {
            "online" -> {
                saySystem("online: ${World.getPlayersCount()}")
            }
            "quit" -> {
                session.logout()
            }
            "give" -> {
                // param 1 - type id || name type
                val typeId: Int = params[1].toIntOrNull() ?: ItemType.fromName(params[1].lowercase()).id

                val newItem = transaction {
                    val e = InventoryItemEntity.new(IdFactory.getNext()) {
                        type = typeId
                        inventoryId = 0
                        this.x = 0
                        this.y = 0
                        quality = 10

                        count = 1
                        tick = 0
                        deleted = false
                    }
                    InventoryItem(e, null)
                }
                if (!inventory.putItem(newItem)) {
                    // TODO new item drop to ground
                }
            }
            "run" -> {
                moveMode = MoveMode.RUN
            }
            "walk" -> {
                moveMode = MoveMode.WALK
            }
            "spawn" -> {
                commandToExecute = cmd
            }
            "tile" -> {
                commandToExecute = cmd
            }
            "restore" -> {
                status.TEST_restore()
            }
            "addtick" -> {
                val t: Long = params[1].toLong()
                TimeController.addTicks(t)
            }
            "off" -> {
                commandToExecute = null
            }
            "iditems" -> {
                ItemType.mapNames.forEach {
                    saySystem("item ${it.key} id=${it.value.id}")
                }
            }
            "help" -> {
                saySystem("List of console commands:")
                saySystem("/online - show count of current online players")
//                saySystem("/quit - logout from server")
                saySystem("/give {id} - spawn inventory item to player's inventory")
                saySystem("/iditems - print available items for /give command")
//                saySystem("/run - change move mode to run")
//                saySystem("/walk - change mode mode to walk")
                saySystem("/addtick {number} - add server's time")
                saySystem("This commands run into \"continuous\" mode:")
                saySystem("/spawn {id} - spawn object my mouse click")
                saySystem("/tile {id} - change tile by mouse click")
                saySystem("/off - disable \"continuous\" console command")
            }
            else ->  {
                saySystem("Unknown command: $cmd")
            }
        }
    }

    /**
     * выполнить ранее сохраненную команду в указанных координатах
     */
    private suspend fun executeCommand(x: Int, y: Int): Boolean {
        val params = commandToExecute!!.split(" ")
        when (params[0]) {
            "spawn" -> {
                // param 1 - type id
                val t: Int = params[1].toInt()
                // param 2 - data for object
                val d = if (params.size >= 3) params[2] else null
                val obj = transaction {
                    val e = EntityObject.makeNew(PositionData(x, y, pos), t)
                    if (d != null) {
                        e.data = d
                    }
                    ObjectsFactory.byEntity(e)
                }
                obj.pos.initGrid()
                val resp = CompletableDeferred<CollisionResult>()
                obj.grid.send(GridMsg.Spawn(obj, resp))
                resp.await()
                return false
            }
            "tile" -> {
                // param 1 - tile type
                val t = params[1].toByte()
                val p = Position(x, y, pos)
                World.getGrid(p).send(GridMsg.SetTile(p, t))
                return false
            }
            "tp" -> {
                // Teleport to
//                if (params.size == 3) {
//                    val x: Int = params[1].toInt()
//                    val y: Int = params[2].toInt()
                // TODO teleport cmd
//                }
                return true
            }
        }
        return true
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

        // TODO broadcast my status to party members
    }

    override fun toString(): String {
        return "${this::class.simpleName} ${appearance.visibleName} [$id] ${pos.point}"
    }
}
