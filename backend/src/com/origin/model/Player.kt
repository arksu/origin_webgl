@file:OptIn(DelicateCoroutinesApi::class)

package com.origin.model

import com.origin.ObjectID
import com.origin.jooq.tables.records.CharacterRecord
import com.origin.model.inventory.Inventory
import com.origin.net.ContextMenuData
import com.origin.net.GameSession
import com.origin.net.MapGridConfirm
import com.origin.util.PLAYER_RECT
import com.origin.util.Rect
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
     * контекстное меню активное в данный момент
     */
    private var contextMenu: ContextMenu? = null

    override suspend fun processMessage(msg: Any) {
        when (msg) {
            is PlayerMessage.Connected -> onConnected()
            is PlayerMessage.Disconnected -> onDisconnected()
            is PlayerMessage.ObjectRightClick -> onObjectRightClick(msg.id)
            else -> super.processMessage(msg)
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