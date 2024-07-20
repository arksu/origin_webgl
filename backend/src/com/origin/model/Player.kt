@file:OptIn(DelicateCoroutinesApi::class)

package com.origin.model

import com.origin.OPEN_DISTANCE
import com.origin.ObjectID
import com.origin.TILE_SIZE
import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.CharacterRecord
import com.origin.jooq.tables.references.CHARACTER
import com.origin.model.inventory.Hand
import com.origin.model.inventory.Inventory
import com.origin.model.inventory.InventoryItem
import com.origin.model.`object`.container.ContainerMessage
import com.origin.move.Move2Object
import com.origin.move.Move2Point
import com.origin.net.*
import com.origin.util.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking

class Player(
    /**
     * персонаж игрока (сущность хранимая в БД)
     */
    val character: CharacterRecord, val session: GameSession
) : Human(
    character.id, ObjectPosition(
        initX = character.x,
        initY = character.y,
        level = character.level,
        region = character.region,
        heading = character.heading
    )
) {
    /**
     * инвентарь игрока
     */
    override val inventory = Inventory(this)

    /**
     * вещь, которую держим в данный момент в руке
     */
    private var hand: Hand? = null

    /**
     * контекстное меню активное в данный момент
     */
    private var contextMenu: ContextMenu? = null

    override val status: PlayerStatus = PlayerStatus(this)

    /**
     * команда для отложенного выполнения (клик по карте)
     */
    var commandToExecuteByMapClick: String? = null

    override suspend fun processMessage(msg: Any) {
        when (msg) {
            is PlayerMessage.Connected -> onConnected()
            is PlayerMessage.Disconnected -> onDisconnected()
            is PlayerMessage.MapClick -> onMapClick(msg)
            is PlayerMessage.ObjectClick -> onObjectClick(msg)
            is PlayerMessage.ObjectRightClick -> onObjectRightClick(msg.id)
            is BroadcastEvent.ChatMessage -> onChatMessage(msg)
            is PlayerMessage.InventoryItemClick -> onItemClick(msg)
            is PlayerMessage.InventoryClose -> onInventoryClose(msg)
            is PlayerMessage.ChatMessage -> onClientChatMessage(msg)
            is PlayerMessage.ContextMenuItem -> onContextMenuItem(msg)
            else -> super.processMessage(msg)
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
//                dropHandItem()
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

    private suspend fun onObjectClick(msg: PlayerMessage.ObjectClick) {
        logger.debug("objectClick $msg")

        if (contextMenu != null) {
            clearContextMenu()
        }
        // знаем ли мы объект по которому шлет клик клиент
        val obj = knownList.getKnownObject(msg.id)
        if (obj != null) {
            // если дистанция между объектом и местом клика меньше порога - считаем что попали в объект
            if (obj.pos.point.dist(Vec2i(msg.x, msg.y)) < 8) {
                // пока просто движемся к объекту
                goAndOpenObject(obj)
            } else if (hand == null) {
                startMove(Move2Point(this, msg.x, msg.y))
            }
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

    private suspend fun onItemClick(msg: PlayerMessage.InventoryItemClick) {
        if (contextMenu != null) clearContextMenu()

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

    private suspend fun onInventoryClose(msg: PlayerMessage.InventoryClose) {
        // это требование закрыть мой инвентарь?
        if (msg.id == id) {
            session.send(InventoryClose(id))
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
                session.send(CreatureSay(msg.obj.id, title, msg.text, msg.channel))
            }
        }
    }

    suspend fun systemSay(text: String) {
        session.send(CreatureSay(id, "System", text, ChatChannel.SYSTEM))
    }

    /**
     * вызывается в самую последнюю очередь при спавне игрока в мир
     * когда уже все прогружено и заспавнено, гриды активированы
     */
    private fun onConnected() {
        World.addPlayer(this)

        // auto save task
//        autoSaveJob = WorkerScope.launch {
//            while (true) {
//                delay(10000L)
//                this@Player.send(PlayerMsg.Store())
//            }
//        }
//        timeUpdateJob = WorkerScope.launch {
//            while (true) {
//                this@Player.session.send(
//                    TimeUpdate(
//                        TimeController.tickCount,
//                        TimeController.getGameHour(),
//                        TimeController.getGameMinute(),
//                        TimeController.getGameDay(),
//                        TimeController.getGameMonth(),
//                        TimeController.getNightValue(),
//                        TimeController.getSunValue(),
//                        0
//                    )
//                )
//                delay(3000L)
//            }
//        }

//        session.send(CraftList(this))
    }

    private suspend fun onDisconnected() {
//        autoSaveJob?.cancel()
//        autoSaveJob = null
//        timeUpdateJob?.cancel()
//        timeUpdateJob = null

        World.removePlayer(this)

//            status.stopRegeneration()
        if (isSpawned) {
            // TODO
            openedObjectsList.closeAll()
            // удалить объект из грида
            remove()
        }
        save()
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
                session.send(ContextMenuData(contextMenu!!))
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
        contextMenu?.processItem(this, msg.item)
        clearContextMenu()
    }

    private suspend fun clearContextMenu() {
        session.send(ContextMenuData(null))
        contextMenu = null
    }

    private suspend fun setHand(item: InventoryItem?, msg: PlayerMessage.InventoryItemClick) {
        hand = if (item != null) {
            val h = Hand(this, item, msg.x, msg.y, msg.ox, msg.oy)
            session.send(HandUpdate(h))
            h
        } else {
            session.send(HandUpdate())
            null
        }
    }

    /**
     * сохранение состояния игрока в базу
     */
    override fun save() {
        logger.debug("store player {}", this)

//        val currentMillis = System.currentTimeMillis()
//        character.onlineTime += TimeUnit.MILLISECONDS.toSeconds(currentMillis - lastOnlineStoreTime)
//
//        status.storeToCharacter(character)
//        lastOnlineStoreTime = currentMillis
    }

    /**
     * сохранение текущей позиции в бд
     */
    override fun storePositionInDb() {
        logger.warn("storePositionInDb ${pos.x} ${pos.y}")
        character.x = pos.x
        character.y = pos.y
        character.level = pos.level
        character.region = pos.region
        character.heading = pos.heading

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

    override fun getBoundRect(): Rect {
        return PLAYER_RECT
    }

    override fun getResourcePath(): String {
        return "player"
    }

    override fun getMaxStamina(): Int {
        return 1000
    }

    override fun broadcastStatusUpdate() {
        val pkt = status.getPacket()
        runBlocking(IO) {
            session.send(pkt)
        }

        // TODO broadcast my status to party members
    }
}