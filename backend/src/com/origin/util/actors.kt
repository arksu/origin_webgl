@file:OptIn(
    DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class
)

package com.origin.util

import kotlinx.coroutines.*

//val WorkerScope = GlobalScope
val WorkerScope = CoroutineScope(Dispatchers.Default)

/**
 * каким диспатчером обрабатываем очереди акторов
 */
val ACTOR_DISPATCHER = Dispatchers.IO.limitedParallelism(50)

/**
 * размер буфера сообщений у акторов
 */
const val ACTOR_BUFFER_CAPACITY = 512

/**
 * сообщение с ожиданием результата
 */
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

abstract class MessageWithJob(
    val job: CompletableJob = Job()
) {
    suspend fun run(block: suspend () -> Unit) {
        block()
        job.complete()
    }
}