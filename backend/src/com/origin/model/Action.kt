package com.origin.model

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@ObsoleteCoroutinesApi
class Action(val me: Human, val target: GameObject, private val ticks: Int, private val block: () -> Unit) {
    companion object {
        /**
         * сколько длится тик для игрового действия
         */
        const val GAME_ACTION_PERIOD = 250L

        val logger: Logger = LoggerFactory.getLogger(Action::class.java)
    }

    private val job: Job = WorkerScope.launch {
        repeat(ticks) { i ->
            logger.debug("before tick $i")
            delay(GAME_ACTION_PERIOD)
            logger.debug("after tick $i")
        }
        me.action = null
        logger.debug("run action")
        block()
    }

    suspend fun stop() {
        logger.debug("stop action")
        job.cancelAndJoin()
        logger.debug("action was stopped")
    }
}