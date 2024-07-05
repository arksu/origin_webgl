package com.origin.util

import com.origin.model.Grid.Companion.logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking

data class Message(
    val ack: CompletableDeferred<Boolean>
)

class Actor {
    val actor = CoroutineScope(ACTOR_DISPATCHER).actor(capacity = ACTOR_BUFFER_CAPACITY) {
        channel.consumeEach {
            try {
                processMessage(it)
            } catch (t: Throwable) {
                logger.error("error while process game object message: ${t.message}", t)
            }
        }
        logger.warn("game obj actor $this finished")
    }

    fun processMessage(m: Any) {
        if (m is Message) {
            m.ack.cancel()
//            throw RuntimeException("1")
//            m.ack.complete(true)
        }
    }
}

fun main() = runBlocking {
    val ack = CompletableDeferred<Boolean>()
    val m = Message(ack)
    val a = Actor()
    a.actor.send(m)

    val r = ack.await()
    println(r)
}