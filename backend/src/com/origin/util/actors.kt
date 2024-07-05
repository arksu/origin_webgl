@file:OptIn(
    DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class
)

package com.origin.util

import kotlinx.coroutines.*

abstract class MessageWithJob(val job: CompletableJob?)

val WorkerScope = GlobalScope

/**
 * каким диспатчером обрабатываем очереди акторов
 */
val ACTOR_DISPATCHER = Dispatchers.IO.limitedParallelism(50)

/**
 * размер буфера сообщений у акторов
 */
const val ACTOR_BUFFER_CAPACITY = 512

abstract class MessageWithAck<T> {
    val ack: CompletableDeferred<T> = CompletableDeferred()

    suspend fun run(block: suspend () -> T) {
        try {
            ack.complete(block())
        } catch (t: Throwable) {
            ack.completeExceptionally(t)
        }
    }
}