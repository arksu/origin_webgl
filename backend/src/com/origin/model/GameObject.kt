@file:OptIn(ObsoleteCoroutinesApi::class)

package com.origin.model

import com.origin.ObjectID
import com.origin.move.CollisionResult
import com.origin.util.ACTOR_BUFFER_CAPACITY
import com.origin.util.ACTOR_DISPATCHER
import com.origin.util.MessageWithAck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class GameObject(val id: ObjectID, val position: ObjectPosition) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Player::class.java)
    }

    /**
     * в каком гриде сейчас находится объект
     * грид указан только тогда, когда берет на себя ответственность за него (заспавнен)
     */
    var grid: Grid? = null

    val isSpawned: Boolean get() = grid != null

    /**
     * актор для обработки сообщений
     */
    private val actor by lazy {
        CoroutineScope(ACTOR_DISPATCHER).actor(capacity = ACTOR_BUFFER_CAPACITY) {
            channel.consumeEach { message ->
                try {
                    processMessage(message)
                } catch (t: Throwable) {
//                    if (message is MessageWithAck) {
//                        message
//                    }
                    logger.error("error while process game object message: ${t.message}", t)
                }
            }
            logger.warn("game obj actor $this finished")
        }
    }

    /**
     * отправить сообщение объекту не дожидаясь ответа
     */
    suspend fun send(msg: Any) {
        actor.send(msg)
    }

    suspend fun <T> sendAndWaitAck(msg: MessageWithAck<T>): T {
        actor.send(msg)
        return msg.ack.await()
    }

    protected open suspend fun processMessage(msg: Any) {
        when (msg) {
            is GameObjectMessage.Spawn -> msg.run { onSpawn(msg.variants) }
            else -> throw RuntimeException("unprocessed actor message ${msg.javaClass.simpleName} $msg")
        }
    }

    private suspend fun onSpawn(variants: List<SpawnType>): Boolean {
        for (variant in variants) {
            val result = when (variant) {
                SpawnType.EXACTLY_POINT -> spawn()
                SpawnType.NEAR -> TODO()
                SpawnType.RANDOM_SAME_REGION -> TODO()
            }
            if (result) return true
        }
        return false
    }

    private suspend fun spawn(): Boolean {
        if (isSpawned) throw RuntimeException("pos.grid is already set, on spawn")

        // берем грид и спавнимся через него
        val g = World.getGrid(position)

        val result = g.sendAndWaitAck(GridMessage.Spawn(this))

        // если успешно добавились в грид - запомним его у себя
        return if (result.result == CollisionResult.CollisionType.COLLISION_NONE) {
            grid = g
            true
        } else {
            false
        }
    }

}