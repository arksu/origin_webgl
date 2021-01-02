package com.origin.model

import kotlinx.coroutines.CompletableJob

abstract class MessageWithJob(val job: CompletableJob?)

const val ACTOR_CAPACITY = 10