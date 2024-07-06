@file:OptIn(DelicateCoroutinesApi::class)

package com.origin.model

import com.origin.jooq.tables.records.CharacterRecord
import com.origin.net.GameSession
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

    override suspend fun processMessage(msg: Any) {
        when (msg) {
            is PlayerMessage.Connected -> onConnected()
            is PlayerMessage.Disconnected -> onDisconnected()
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
}