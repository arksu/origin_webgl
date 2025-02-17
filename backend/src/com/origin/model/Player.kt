package com.origin.model

import com.origin.OPEN_DISTANCE
import com.origin.ObjectID
import com.origin.TILE_SIZE
import com.origin.TimeController
import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.CharacterRecord
import com.origin.jooq.tables.references.CHARACTER
import com.origin.model.action.Action
import com.origin.model.craft.CraftFactory
import com.origin.model.inventory.Hand
import com.origin.model.inventory.Inventory
import com.origin.model.item.Item
import com.origin.model.item.ItemFactory
import com.origin.model.kind.Inner
import com.origin.model.kind.Liftable
import com.origin.model.kind.Openable
import com.origin.model.message.GridMessage
import com.origin.model.message.PlayerMessage
import com.origin.model.`object`.GameObject
import com.origin.model.`object`.container.ContainerMessage
import com.origin.move.Move2Object
import com.origin.move.Move2Point
import com.origin.net.*
import com.origin.util.ClientButton
import com.origin.util.Rect
import com.origin.util.SHIFT_KEY
import com.origin.util.Vec2i
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class Player(
    /**
     * персонаж игрока (сущность хранимая в БД)
     */
    val character: CharacterRecord,

    /**
     * игровая сессия (сокет), если null - значит игрок в мире, но отвязан от сокета (detached)
     */
    var socket: GameSocket?
) : Human(
    character.id, ObjectPosition(
        initX = character.x,
        initY = character.y,
        level = character.level,
        region = character.region,
        heading = character.heading
    )
), Inner {
    /**
     * инвентарь игрока
     */
    override val inventory = Inventory(this)

    /**
     * скиллы игрока и их уровень
     */
    val skills = SkillsList(this)

    /**
     * актуальный список крафтов доступный игроку
     */
    val crafts = CraftFactory.forPlayer(this)

    /**
     * вещь, которую держим в данный момент в руке
     */
    var hand: Hand? = Hand.load(this)
        private set

    /**
     * объект, который переносим над собой
     */
    var lift: GameObject? = null
        private set

    /**
     * текущий курсор на клиенте
     */
    var cursor: Cursor = Cursor.DEFAULT
        private set

    /**
     * контекстное меню активное в данный момент
     */
    private var contextMenu: ContextMenu? = null

    override val status: PlayerStatus = PlayerStatus(this)

    /**
     * команда для отложенного выполнения (клик по карте)
     */
    var commandToExecuteByMapClick: String? = null

    /**
     * время последнего сохранения в базу добавления времени онлайна
     */
    private var lastOnlineStoreTime = System.currentTimeMillis()

    /**
     * обратный отсчет на отключение от мира (в тиках регенерации)
     */
    @Volatile
    private var detachWorldCountdown: Int = 0

    override suspend fun processMessage(msg: Any) {
        when (msg) {
            is PlayerMessage.Connected -> onConnected()
            is PlayerMessage.Disconnected -> onDisconnected()
            is PlayerMessage.Attach -> msg.run { onAttach(msg) }
            is PlayerMessage.KeyDown -> onKeyDown(msg)
            is PlayerMessage.MapClick -> onMapClick(msg)
            is PlayerMessage.ObjectClick -> onObjectClick(msg)
            is PlayerMessage.ObjectRightClick -> onObjectRightClick(msg.id)
            is BroadcastEvent.ChatMessage -> onChatMessage(msg)
            is PlayerMessage.InventoryItemClick -> onItemClick(msg)
            is PlayerMessage.InventoryRightItemClick -> onItemRightClick(msg)
            is PlayerMessage.InventoryClose -> onInventoryClose(msg)
            is PlayerMessage.ChatMessage -> onClientChatMessage(msg)
            is PlayerMessage.ContextMenuItem -> onContextMenuItem(msg)
            is PlayerMessage.Craft -> onCraft(msg)
            is PlayerMessage.Action -> onAction(msg)
            else -> super.processMessage(msg)
        }
    }

    private suspend fun onKeyDown(msg: PlayerMessage.KeyDown) {
        if (msg.key == "escape") {
            setCursor(Cursor.DEFAULT)
        }
    }

    private suspend fun onMapClick(msg: PlayerMessage.MapClick) {
        logger.debug("mapClick {}", msg)

        if (contextMenu != null) {
            clearContextMenu()
        }

        if (msg.btn == ClientButton.LEFT) {
            // если что-то держим в руке надо дропнуть это
            if (hand != null) {
                dropHandItem()
            } else if (cursor != Cursor.DEFAULT) {
                setCursor(Cursor.DEFAULT)
            } else {
                val cmd = commandToExecuteByMapClick
                if (cmd != null) {
                    if (msg.flags and SHIFT_KEY > 0) {
                        logger.warn("SHIFT")
                        val xx = msg.x / TILE_SIZE * TILE_SIZE + TILE_SIZE / 2
                        val yy = msg.y / TILE_SIZE * TILE_SIZE + TILE_SIZE / 2
                        PlayerCommands.runCommandByMapClick(this, cmd, xx, yy)
                    } else PlayerCommands.runCommandByMapClick(this, cmd, msg.x, msg.y)
                    commandToExecuteByMapClick = null
                } else {
                    startMove(Move2Point(this, msg.x, msg.y))
                }
            }
        }
    }

    /**
     * клик по объекту на карте
     */
    private suspend fun onObjectClick(msg: PlayerMessage.ObjectClick) {
        logger.debug("objectClick {}", msg)

        if (contextMenu != null) {
            clearContextMenu()
        }
        // знаем ли мы объект по которому шлет клик клиент
        val obj = knownList.getKnownObject(msg.id)
        if (obj != null) {
            // если дистанция между объектом и местом клика меньше порога - считаем что попали в объект
            if (obj.pos.point.dist(Vec2i(msg.x, msg.y)) < 8) {
                if (cursor == Cursor.LIFT) {
                    if (obj is Liftable) {
                        setCursor(Cursor.DEFAULT)
                        goAndLiftObject(obj)
                    }
                }
                // если объект можно открыть
                else if (obj is Openable) {
                    goAndOpenObject(obj)
                } else {
                    // просто движемся к объекту
                    goToObject(obj)
                }
            } else if (hand == null) {
                startMove(Move2Point(this, msg.x, msg.y))
            }
        }
    }

    private suspend fun goToObject(obj: GameObject) {
        // проверим расстояние от меня до объекта
        val myRect = getBoundRect().clone().move(pos.point)
        val objRect = obj.getBoundRect().clone().move(obj.pos.point)
        val (mx, my) = myRect.min(objRect)
        logger.debug("goToObject min $mx $my")
        if (mx > OPEN_DISTANCE || my > OPEN_DISTANCE) {
            startMove(
                Move2Object(this, obj)
            )
        }
    }

    private suspend fun goAndOpenObject(obj: GameObject) {
        // проверим расстояние от меня до объекта
        val myRect = getBoundRect().clone().move(pos.point)
        val objRect = obj.getBoundRect().clone().move(obj.pos.point)
        val (mx, my) = myRect.min(objRect)
        logger.debug("goAndOpenObject min $mx $my")
        // TODO : проверка hand - положить вещь в инвентарь, или наполнить его (дрова, вода и тд)
        if (mx <= OPEN_DISTANCE && my <= OPEN_DISTANCE) {
            openedObjectsList.open(obj)
        } else {
            startMove(
                Move2Object(this, obj) {
                    openedObjectsList.open(obj)
                }
            )
        }
    }

    private suspend fun goAndLiftObject(obj: GameObject) {
        // проверим расстояние от меня до объекта
        val myRect = getBoundRect().clone().move(pos.point)
        val objRect = obj.getBoundRect().clone().move(obj.pos.point)
        val (mx, my) = myRect.min(objRect)
        logger.debug("goAndLiftObject min $mx $my")
        if (mx <= OPEN_DISTANCE && my <= OPEN_DISTANCE) {
            setLift(obj)
        } else {
            startMove(
                Move2Object(this, obj) {
                    setLift(obj)
                }
            )
        }
    }

    private suspend fun setLift(obj: GameObject?) {
        if (obj != null && obj !is Liftable) {
            throw IllegalStateException("set lift with non liftable object")
        }
        val oldLiftObject = lift
        if (oldLiftObject != null && obj != null) {
            throw IllegalStateException("try set lift when already have lifting object")
        }
        if (oldLiftObject == null && obj == null) {
            throw IllegalStateException("try down lift when no lifting object")
        }
        lift = obj
        if (obj != null) {
            // отправить клиенту пакет на лифт объекта,
            // перемещающий объект в список переносимых игроком
            sendToSocket(ObjectAddPacket.build(obj))

            // убрать из грида объект, теперь игрок на него отвечает (хэндлит его, обновляет его координаты в базе, спавнится вместе с ним)
            // целиком удаляет из грида, из known list, и с клиента
            obj.getGridSafety().send(GridMessage.RemoveObject(obj))
        } else {
            // должны явно что-то положить на землю, был объект который перетаскивали
            if (oldLiftObject != null) {
                oldLiftObject.pos.level = pos.level
                oldLiftObject.pos.region = pos.region
                oldLiftObject.setXY(pos.x, pos.y)

                // при спавне будет отправлен ObjectAdd
                val spawned = oldLiftObject.getGridSafety().sendAndWaitAck(GridMessage.Spawn(oldLiftObject))

                if (spawned) {
                    // если удалось положить объект - отправим апдейт себя где уже нет lift
                    sendToSocket(ObjectAddPacket.build(this))
                }
            }
        }
    }

    override fun getInnerObjects(): List<GameObject>? {
        val l = lift
        return if (l != null) listOf(l) else null
    }

    /**
     * клик по вещи в инвентаре
     */
    private suspend fun onItemClick(msg: PlayerMessage.InventoryItemClick) {
        if (contextMenu != null) clearContextMenu()
        setCursor(Cursor.DEFAULT)

        // держим в руке что-то?
        val h = hand
        if (h == null) {
            // в руке ничего нет. возьмем из инвентаря
            val taken = if (msg.inventoryId == id) {
                inventory.takeItem(msg.id)
            } else {
                val obj = openedObjectsList.get(msg.inventoryId)
                // возьмем из объекта вещь
                obj?.sendAndWaitAck(ContainerMessage.TakeItem(this, msg.id))
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
                    val obj = openedObjectsList.get(msg.inventoryId)
                    obj?.sendAndWaitAck(ContainerMessage.PutItem(h.item, msg.x - h.offsetX, msg.y - h.offsetY)) ?: false
                }
                if (success) {
                    setHand(null, msg)
                }
            }
        }
    }

    private suspend fun onItemRightClick(msg: PlayerMessage.InventoryRightItemClick) {
        if (contextMenu != null) clearContextMenu()
        setCursor(Cursor.DEFAULT)

        if (msg.inventoryId == id) {
            contextMenu = inventory.items[msg.id]?.getContextMenu(this)
        } else {
            val obj = openedObjectsList.get(msg.inventoryId)
            contextMenu = obj?.inventory?.items?.get(msg.id)?.getContextMenu(this)
        }
        if (contextMenu != null) {
            sendToSocket(ContextMenuData(contextMenu))
        }
    }

    private suspend fun onInventoryClose(msg: PlayerMessage.InventoryClose) {
        // это требование закрыть мой инвентарь?
        if (msg.id == id) {
            sendToSocket(InventoryClose(id))
        } else {
            openedObjectsList.close(msg.id)
        }
    }

    private suspend fun onClientChatMessage(msg: PlayerMessage.ChatMessage) {
        if (msg.text.startsWith("/")) {
            // удаляем слеш в начале строки и заускаем на выполнение
            PlayerCommands.runCommand(this, msg.text.substring(1))
        } else {
            getGridSafety().broadcast(BroadcastEvent.ChatMessage(this, ChatChannel.GENERAL, msg.text))
        }
    }

    private suspend fun onChatMessage(msg: BroadcastEvent.ChatMessage) {
        if (msg.channel == ChatChannel.GENERAL) {
            if (knownList.isKnownObject(msg.obj)) {
                val title = if (msg.obj is Player) msg.obj.character.name else "unk"
                sendToSocket(CreatureSay(msg.obj.id, title, msg.text, msg.channel))
            }
        }
    }

    private suspend fun onCraft(msg: PlayerMessage.Craft) {
        // ищем выбранный крафт в списке доступных
        val craft = crafts[msg.name]
        if (craft != null) {
            // проверим что есть все необходимые ингридиенты в инвентаре
            if (inventory.findAndTakeItem(craft, false) == null) {
                systemSay("not enough items to craft ${craft.name}")
                return
            }

            // craft action
            action = object : Action(this@Player) {
                override val ticks = craft.ticks
                override val staminaConsume = craft.staminaConsume
                override val minimumStaminaRequired = craft.minimumStaminaRequired

                override suspend fun run(): Boolean {
                    // ищем и берем вещи из инвентаря. вернет список только если все требуемые вещи есть в полном объеме в инвентаре
                    val takenItems = inventory.findAndTakeItem(craft, true)
                    if (takenItems != null) {
                        // удалим их навсегда из игры, поскольку они используются для крафта вещи
                        takenItems.forEach {
                            it.delete()
                        }
                        // создаем вещи по рецепту из крафта в нужном количестве
                        craft.produce.forEach {
                            var left = it.value
                            while (left > 0) {
                                left--
                                val q: Short = craft.calcQuality(takenItems)
                                // создаем новую вещь из списка produced крафта
                                val item = ItemFactory.create(it.key, quality = q)
                                // пытаемся положить вещь в наш инвентарь, если не удается - прекращаем спавн вещей
                                if (!inventory.spawnItem(item)) return@forEach
                            }
                        }
                    } else {
                        systemSay("not enough items to craft ${craft.name}")
                    }
                    return true
                }
            }
        }
    }

    private suspend fun onAction(msg: PlayerMessage.Action) {
        // TODO
        if (msg.name == "lift") {
            setCursor(Cursor.LIFT)
        }
    }

    private suspend fun dropHandItem() {
        // TODO dropHandItem
    }

    suspend fun systemSay(text: String) {
        sendToSocket(CreatureSay(id, "System", text, ChatChannel.SYSTEM))
    }

    /**
     * вызывается в самую последнюю очередь при спавне игрока в мир
     * когда уже все прогружено и заспавнено, гриды активированы
     */
    private suspend fun onConnected() {
        World.addPlayer(this)
        sendTimeUpdate()
        sendToSocket(CraftListPacket(crafts))

        hand?.let { sendToSocket(HandUpdate(it)) }
    }

    private suspend fun onAttach(msg: PlayerMessage.Attach): Boolean {
        // не можем подключить новый сокет к игроку если у нас уже есть активный сокет
        if (socket != null) return false
        socket = msg.socket

        grids.forEach {
            sendToSocket(MapGridData(it, MapGridData.Type.ADD))
        }
        sendToSocket(MapGridConfirm())
        knownList.resendObjectAdd()
        broadcastStatusUpdate()
        // смотри afterSpawn()!!!
        return true
    }

    private fun onDisconnected() {
        // отвязываемся от игрового сокета
        socket = null

        // запускаем таймер на отключение от мира
        detachWorldCountdown = 10
    }

    override suspend fun loadGrids() {
        super.loadGrids()
        sendToSocket(MapGridConfirm())
    }

    override suspend fun onGridChanged() {
        super.onGridChanged()
        sendToSocket(MapGridConfirm())
    }

    /**
     * правый клик по объекту
     */
    private suspend fun onObjectRightClick(id: ObjectID) {
        logger.debug("objectRightClick $id")
        if (hand != null) return

        // если уже есть активное контекстное меню на экране
        if (contextMenu != null) {
            // пошлем отмену КМ
            clearContextMenu()
        } else {
            // попробуем вызывать КМ у объекта
            val obj = knownList.getKnownObject(id)
            contextMenu = obj?.openContextMenu(this)
            // если у объекта есть контекстное меню
            if (contextMenu != null) {
                sendToSocket(ContextMenuData(contextMenu!!))
            } else {
                if (obj != null) {
                    goAndOpenObject(obj)
                }
            }
        }
    }

    /**
     * выбран пункт контекстного меню объекта
     */
    private suspend fun onContextMenuItem(msg: PlayerMessage.ContextMenuItem) {
        contextMenu?.execute(this, msg.item)
        clearContextMenu()
    }

    private suspend fun clearContextMenu() {
        sendToSocket(ContextMenuData(null))
        contextMenu = null
    }

    suspend fun setHand(item: Item?, msg: PlayerMessage.InventoryItemClick) {
        hand = if (item != null) {
            val h = Hand(this, item, msg.x, msg.y, msg.ox, msg.oy)
            sendToSocket(HandUpdate(h))
            h
        } else {
            sendToSocket(HandUpdate())
            null
        }
    }

    private suspend fun setCursor(c: Cursor) {
        if (cursor != c) {
            cursor = c
            sendToSocket(CursorPacket(cursor))
        }
    }

    /**
     * сохранение состояния игрока в базу
     */
    override fun save() {
        logger.debug("store player {}", this)
        status.save()

        val currentMillis = System.currentTimeMillis()
        val delta = currentMillis - lastOnlineStoreTime
        if (delta >= 1000) {
            val secs = delta / 1000
            character.onlineTime += secs
            lastOnlineStoreTime += secs * 1000
        }

        DatabaseConfig.dsl
            .update(CHARACTER)
            .set(CHARACTER.X, character.x)
            .set(CHARACTER.Y, character.y)
            .set(CHARACTER.LEVEL, character.level)
            .set(CHARACTER.REGION, character.region)
            .set(CHARACTER.HEADING, character.heading)
            .set(CHARACTER.STAMINA, character.stamina)
            .set(CHARACTER.ONLINE_TIME, character.onlineTime)
            .where(CHARACTER.ID.eq(character.id))
            .execute()
    }

    /**
     * сохранение текущей позиции в бд
     */
    override suspend fun storePositionInDb() {
        logger.warn("storePositionInDb ${pos.x} ${pos.y}")
        character.x = pos.x
        character.y = pos.y
        character.level = pos.level
        character.region = pos.region
        character.heading = pos.heading

        withContext(IO) {
            DatabaseConfig.dsl
                .update(CHARACTER)
                .set(CHARACTER.X, character.x)
                .set(CHARACTER.Y, character.y)
                .set(CHARACTER.LEVEL, character.level)
                .set(CHARACTER.REGION, character.region)
                .set(CHARACTER.HEADING, character.heading)
                .where(CHARACTER.ID.eq(character.id))
                .execute()
        }

        super.storePositionInDb()
    }

    /**
     * call from outside
     */
    suspend fun updateRegeneration() {
        status.regeneration()
        // если сокет уже отвязан и есть таймер обратного отсчета
        if (socket == null && detachWorldCountdown > 0) {
            detachWorldCountdown--
            // если обратный отсчет закончился
            if (detachWorldCountdown <= 0) {
                detachWorld()
            }
        }
    }

    private suspend fun detachWorld() {
        World.removePlayer(this)

        if (isSpawned) {
            // удалить объект из грида
            remove()
        }
        save()
    }

    override suspend fun onRemovedFromGrid() {
        openedObjectsList.closeAll()
        val l = lift
        if (l != null) {
            getGridSafety().sendAndWaitAck(GridMessage.Spawn(l))
        }
        super.onRemovedFromGrid()
    }

    override fun getBoundRect(): Rect {
        return Rect.PLAYER_RECT
    }

    override fun getResourcePath(): String {
        return "player"
    }

    override fun broadcastStatusUpdate() {
        val pkt = status.getPacket()
        runBlocking(IO) {
            sendToSocket(pkt)
        }

        // TODO broadcast my status to party members
    }

    suspend fun sendTimeUpdate() {
        sendToSocket(
            TimeUpdate(
                t = TimeController.tickCount,
                h = TimeController.getGameHour(),
                m = TimeController.getGameMinute(),
                d = TimeController.getGameDay(),
                mm = TimeController.getGameMonth(),
                nv = TimeController.getNightValue(),
                sv = TimeController.getSunValue(),
                mv = 0 // TODO moon value
            )
        )
    }

    suspend fun sendToSocket(message: ServerMessage) {
        socket?.send(message)
    }
}