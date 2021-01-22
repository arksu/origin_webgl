package com.origin.model

import com.origin.TimeController
import com.origin.collision.CollisionResult
import com.origin.entity.Character
import com.origin.entity.EntityObject
import com.origin.idfactory.IdFactory
import com.origin.model.BroadcastEvent.ChatMessage.Companion.SYSTEM
import com.origin.model.move.Move2Object
import com.origin.model.move.Move2Point
import com.origin.model.move.MoveMode
import com.origin.net.model.CreatureSay
import com.origin.net.model.GameSession
import com.origin.utils.ObjectID
import com.origin.utils.Rect
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerMsg {
    class Connected
    class Disconnected
    class MapClick(val x: Int, val y: Int)
    class ObjectClick(val id: ObjectID)
    class ObjectRightClick(val id: ObjectID)
}

private val PLAYER_RECT = Rect(10)

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
            is PlayerMsg.ObjectClick -> objectClick(msg.id)
            is PlayerMsg.ObjectRightClick -> objectRightClick(msg.id)
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

    /**
     * клик по объекту в мире
     */
    private suspend fun objectClick(id: ObjectID) {
        logger.debug("objectClick $id")
        val obj = knownList.getKnownObject(id)
        if (obj != null) {
            // пока просто движемся к объекту
            startMove(Move2Object(this, obj))
        }
    }

    private suspend fun objectRightClick(id: ObjectID) {
        logger.debug("objectRightClick $id")

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
                val obj = transaction {
                    val o = EntityObject.new(IdFactory.getNext()) {
                        x = pos.x
                        y = pos.y
                        gridx = pos.gridX
                        gridy = pos.gridY
                        level = pos.level
                        region = pos.region
                        heading = 0
                        type = 23
                        quality = 10
                        createTick = TimeController.tickCount
                    }
                    val obj = StaticObject(o)
                    obj
                }
                val resp = CompletableDeferred<CollisionResult>()
                grid.send(GridMsg.Spawn(obj, resp))
            }

            "spawn2" -> {
                val obj = transaction {
                    val o = EntityObject.new(IdFactory.getNext()) {
                        x = pos.x
                        y = pos.y
                        gridx = pos.gridX
                        gridy = pos.gridY
                        level = pos.level
                        region = pos.region
                        heading = 0
                        type = 1
                        quality = 10
                        createTick = TimeController.tickCount
                    }
                    val obj = StaticObject(o)
                    obj
                }
                val resp = CompletableDeferred<CollisionResult>()
                grid.send(GridMsg.Spawn(obj, resp))
            }
        }
    }
}