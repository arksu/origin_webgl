package com.origin.model

import com.origin.entity.Character
import com.origin.model.BroadcastEvent.ChatMessage.Companion.SYSTEM
import com.origin.model.move.Move2Point
import com.origin.model.move.MoveMode
import com.origin.net.model.CreatureSay
import com.origin.net.model.GameSession
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
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
) : Human(character.id.value, character.x, character.y, character.level, character.region, character.heading) {

    private var moveMode = MoveMode.WALK

    /**
     * одежда (во что одет игрок)
     */
    private val paperdoll: Paperdoll = Paperdoll(this)

    override suspend fun processMessage(msg: Any) {
        logger.debug("Player $this msg ${msg.javaClass.simpleName}")
        when (msg) {
            is PlayerMsg.Connected -> connected()
            is PlayerMsg.Disconnected -> disconnected()
            is PlayerMsg.MapClick -> mapClick(msg.x, msg.y)
            is BroadcastEvent.ChatMessage -> chatMessage(msg)

            else -> super.processMessage(msg)
        }
    }

    /**
     * клиент: клик по карте
     */
    private suspend fun mapClick(x: Int, y: Int) {
        logger.debug("mapClick $x $y")
        startMove(Move2Point(this, x, y))
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
    }

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
     * обработка команд в консоли
     */
    suspend fun consoleCommand(cmd: String) {
        logger.warn("adminCommand $cmd")
        when (cmd) {
            "online" -> {
                session.send(CreatureSay(0, "online: ${World.getPlayersCount()}", SYSTEM))
            }
            "spawn" -> {

            }
        }
    }
}