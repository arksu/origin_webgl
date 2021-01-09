package com.origin.model

import com.origin.entity.Character
import com.origin.model.move.Move2Point
import com.origin.net.model.GameSession
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerMsg {
    class Connected
    class Disconnected
    class MapClick(val x: Int, val y: Int)
}

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

    enum class State {
        None, Connected, Disconnected
    }

    var state = State.None

    /**
     * одежда (во что одет игрок)
     */
    val paperdoll: Paperdoll = Paperdoll(this)

    override suspend fun processMessage(msg: Any) {
        logger.debug("Player $this msg ${msg.javaClass.simpleName}")
        when (msg) {
            is PlayerMsg.Connected -> connected()
            is PlayerMsg.Disconnected -> disconnected()
            is PlayerMsg.MapClick -> mapClick(msg.x, msg.y)
            else -> super.processMessage(msg)
        }
    }

    /**
     * клиент: клик по карте
     */
    private fun mapClick(x: Int, y: Int) {
        logger.debug("mapClick $x $y")
        startMove(Move2Point(this, x, y))
    }

    /**
     * вызывается в самую последнюю очередь при спавне игрока в мир
     * когда уже все прогружено и заспавнено, гриды активированы
     */
    private fun connected() {
        state = State.Connected
        World.addPlayer(this)
    }

    /**
     * игровой клиент (аккаунт) отключился от игрока
     */
    private suspend fun disconnected() {
        if (state == State.Disconnected) return

        World.removePlayer(this)

        // deactivate and unload grids
        unloadGrids()
        // удалить объект из мира
        remove()

        state = State.Disconnected
    }

    override fun storePositionInDb() {
        character.x = pos.x
        character.y = pos.y
        character.level = pos.level
        character.region = pos.region
        character.heading = pos.heading

        transaction {
            character.flush()
        }
    }
}