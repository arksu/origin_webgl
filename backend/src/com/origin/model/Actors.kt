package com.origin.model

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers

abstract class MessageWithJob(val job: CompletableJob?)

val ACTOR_DISPATCHER = Dispatchers.IO

const val ACTOR_BUFFER_CAPACITY = 10