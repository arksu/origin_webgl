package com.origin.model

import com.origin.entity.Character
import com.origin.net.model.GameSession
import kotlinx.coroutines.ObsoleteCoroutinesApi

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
) : Human(character) {

    override suspend fun processMessages(msg: Any) {
        when (msg) {
            else -> super.processMessages(msg)
        }
    }

    /**
     * одежда (во что одет игрок)
     */
    val paperdoll: Paperdoll = Paperdoll(this)

    suspend fun disconnected() {
        // поскольку это отключение - поэтому ждать завершения обработки сообщений ни к чему
        // deactivate and unload grids
        actor.send(MovingObjectMsg.UnloadGrids())
        // удалить объект из мира
        actor.send(GameObjectMsg.Remove())
    }

}