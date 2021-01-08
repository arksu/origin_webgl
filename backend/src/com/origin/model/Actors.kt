package com.origin.model

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers

abstract class MessageWithJob(val job: CompletableJob?)

/**
 * каким диспатчером обрабатываем очереди акторов
 */
val ACTOR_DISPATCHER = Dispatchers.IO

/**
 * размер буфера сообщений у акторов
 */
const val ACTOR_BUFFER_CAPACITY = 10