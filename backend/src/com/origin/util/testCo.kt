package com.origin.util

import kotlinx.coroutines.*
import kotlin.system.*
import kotlinx.coroutines.channels.*
import java.util.concurrent.atomic.AtomicBoolean


suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 800  // number of coroutines to launch
    val k = 2000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
        coroutineScope {
            repeat(n) {
                launch {
                    repeat(k) { action() }
                }
            }
        }
    }
    println("Completed ${n * k} actions in $time ms")
}

interface ActorContext

abstract class StatefulActor(
    private val parentScope: CoroutineScope,
    bufferCapacity: Int = 64
) {
    private val dispatchChannel = Channel<suspend ActorContext.() -> Unit>(bufferCapacity)
    private val initialized = AtomicBoolean(false)

    private fun initializeIfNeeded() {
        if(initialized.compareAndSet(false, true)) {
            parentScope.launch {
                for (action in dispatchChannel) {
                    launch { context.action() }
                }
            }
        }
    }

    fun shutdown() {
        dispatchChannel.close()
    }

    protected fun act(action: suspend ActorContext.() -> Unit) {
        initializeIfNeeded()
        dispatchChannel.trySendBlocking(action)
    }

    abstract val context: ActorContext
}

class MyActor(scope: CoroutineScope) : StatefulActor(scope), ActorContext {
    private var clickCounter = 0

    override val context: ActorContext get() = this

    fun increment() {
        act { clickCounter++ }
    }

    fun printCount() {
        act { println(clickCounter) }
    }
}

fun main() = runBlocking {
    val actor = MyActor(this)
    massiveRun {
        actor.increment()
    }
    actor.printCount()
    actor.shutdown()
}