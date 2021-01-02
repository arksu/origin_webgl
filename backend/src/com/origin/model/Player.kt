package com.origin.model

import com.origin.entity.Character
import com.origin.model.GameObjectMsg.Remove
import com.origin.model.MovingObjectMsg.UnloadGrids
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
) : Human(character.id.value, character) {

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
        World.removePlayer(this)

        // deactivate and unload grids
        sendJobAndJoin(UnloadGrids::class)
        // удалить объект из мира
        sendJobAndJoin(Remove::class)
        // завершаем актора
        actor.close()
    }

}