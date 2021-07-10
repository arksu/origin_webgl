package com.origin.model

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

abstract class MessageWithJob(val job: CompletableJob?)

@DelicateCoroutinesApi
val WorkerScope = GlobalScope

/**
 * каким диспатчером обрабатываем очереди акторов
 */
val ACTOR_DISPATCHER = Dispatchers.IO

/**
 * размер буфера сообщений у акторов
 */
const val ACTOR_BUFFER_CAPACITY = 512
