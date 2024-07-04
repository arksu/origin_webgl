@file:OptIn(ObsoleteCoroutinesApi::class)

package com.origin.model

import com.origin.ObjectID
import com.origin.move.CollisionResult
import com.origin.util.ACTOR_BUFFER_CAPACITY
import com.origin.util.ACTOR_DISPATCHER
import kotlinx.coroutines.CompletableDeferred
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

    var isSpawned = false
        private set

    /**
     * в каком гриде сейчас находится объект
     */

    var grid: Grid? = null

    /**
     * актор для обработки сообщений
     */
    private val actor = CoroutineScope(ACTOR_DISPATCHER).actor<Any>(capacity = ACTOR_BUFFER_CAPACITY) {
        channel.consumeEach {
            try {
//                processMessage(it)
            } catch (t: Throwable) {
                logger.error("error while process game object message: ${t.message}", t)
            }
        }
        logger.warn("game obj actor $this finished")
    }

    suspend fun spawn(): Boolean {
        if (isSpawned) throw RuntimeException("pos.grid is already set, on spawn")

        // берем грид и спавнимся через него
        val g = World.getGrid(position)

        val resp = CompletableDeferred<CollisionResult>()
//        g.send(GridMsg.Spawn(this, resp))
        val result = resp.await()

        // если успешно добавились в грид - запомним его у себя
        return if (result.result == CollisionResult.CollisionType.COLLISION_NONE) {
            position.setGrid(g)
            isSpawned = true
            true
        } else {
            false
        }
    }

}