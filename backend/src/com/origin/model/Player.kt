@file:OptIn(DelicateCoroutinesApi::class)

package com.origin.model

import com.origin.OPEN_DISTANCE
import com.origin.ObjectID
import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.CharacterRecord
import com.origin.jooq.tables.references.CHARACTER
import com.origin.model.inventory.Hand
import com.origin.model.inventory.Inventory
import com.origin.model.inventory.InventoryItem
import com.origin.move.Move2Object
import com.origin.move.Move2Point
import com.origin.net.*
import com.origin.util.ClientButton
import com.origin.util.PLAYER_RECT
import com.origin.util.Rect
import com.origin.util.Vec2i
import kotlinx.coroutines.DelicateCoroutinesApi

class Player(
    /**
     * персонаж игрока (сущность хранимая в БД)
     */
    private val character: CharacterRecord, val session: GameSession
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

    override suspend fun processMessage(msg: Any) {
        when (msg) {
            is PlayerMessage.Connected -> onConnected()
            is PlayerMessage.Disconnected -> onDisconnected()
            is PlayerMessage.MapClick -> onMapClick(msg)
            is PlayerMessage.ObjectClick -> onObjectClick(msg)
            is PlayerMessage.ObjectRightClick -> onObjectRightClick(msg.id)
            is BroadcastEvent.ChatMessage -> onChatMessage(msg)
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
//                if (commandToExecute != null) {
//                    if (flags and SHIFT_KEY > 0) {
//                        logger.warn("SHIFT")
//                        val xx = x / TILE_SIZE * TILE_SIZE + TILE_SIZE / 2
//                        val yy = y / TILE_SIZE * TILE_SIZE + TILE_SIZE / 2
//                        if (executeCommand(xx, yy)) commandToExecute = null
//                    } else if (executeCommand(x, y)) commandToExecute = null
//                } else {
                startMove(Move2Point(this, msg.x, msg.y))
//                }
            }
        }
    }

    private suspend fun onObjectClick(msg: PlayerMessage.ObjectClick) {
        logger.debug("objectClick $id")

        if (contextMenu != null) {
            clearContextMenu()
        }
        val obj = knownList.getKnownObject(id)
        if (obj != null) {
            // если дистанция между объектом и местом клика меньше порога - считаем что попали в объект
            if (obj.pos.point.dist(Vec2i(msg.x, msg.y)) < 10) {
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

        if (isSpawned) {
            // TODO
//            status.stopRegeneration()
//            openObjectsList.closeAll()
            // удалить объект из грида
            remove()
        }
        save()
    }

    private suspend fun onChatMessage(msg: BroadcastEvent.ChatMessage) {
        if (msg.channel == ChatChannel.GENERAL) {
            if (knownList.isKnownObject(msg.obj)) {
                val title = if (msg.obj is Player) msg.obj.character.name else "unk"
                session.send(CreatureSay(msg.obj.id, title, msg.text, msg.channel))
            }
        }
    }

    override suspend fun loadGrids() {
        super.loadGrids()
        session.send(MapGridConfirm())
    }

    override suspend fun onGridChanged() {
        super.onGridChanged()
        session.send(MapGridConfirm())
    }

    private fun onObjectRightClick(id: ObjectID) {

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
     * вырбан пункт контекстного меню
     */
    private suspend fun contextMenuItem(item: String) {
        contextMenu?.processItem(this, item)
        contextMenu = null
    }

    private suspend fun clearContextMenu() {
        session.send(ContextMenuData(null))
        contextMenu = null
    }

    /**
     * сохранение состояния игрока в базу
     */
    private fun save() {
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

    override fun broadcastStatusUpdate() {
        // TODO

//        val su = StatusUpdate(this)
//        su.addAttribute(CUR_SHP, status.currentSoftHp.roundToInt())
//        su.addAttribute(CUR_HHP, status.currentHardHp.roundToInt())
//        su.addAttribute(MAX_HP, getMaxHp().roundToInt())
//
//        su.addAttribute(CUR_STAMINA, status.currentStamina.roundToInt())
//        su.addAttribute(MAX_STAMINA, getMaxStamina().roundToInt())
//
//        su.addAttribute(CUR_ENERGY, status.currentEnergy.roundToInt())
//        su.addAttribute(MAX_ENERGY, getMaxEnergy().roundToInt())
//
//        runBlocking(IO) {
//            session.send(su)
//        }

        // TODO broadcast my status to party members
    }
}