package com.origin.model

import com.origin.net.model.ActionProgress
import com.origin.net.model.ServerMessage
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@ObsoleteCoroutinesApi
class Action(
    val me: Human,
    val target: GameObject,
    private val ticks: Int,
    private val startProgress: Int,
    val maxProgress: Int,
    private val playerCondition: ((Player) -> Boolean)?,
    private val block: suspend (Action) -> Boolean,
) {
    companion object {
        /**
         * сколько длится тик для игрового действия
         */
        const val GAME_ACTION_PERIOD = 250L

        val logger: Logger = LoggerFactory.getLogger(Action::class.java)
    }

    private val job: Job = WorkerScope.launch {
        sendPkt(ActionProgress(startProgress, maxProgress))

        // повтоярем циклы задержки и выполнения блока
        while (true) {
            if (playerCondition != null) {
                // ДО каждого тика проверяем выполнение условий на игроке (стамина, голод и тд)
                val playerResult = CompletableDeferred<Boolean>()
                me.send(PlayerMsg.ExecuteActionCondition(playerResult, playerCondition))
                // условие на игроке вернуло ложь. значит не выполнилось условие для следующего тика действия
                if (!playerResult.await()) {
                    break
                }
            }

            // ждем нужное количество тиков чтобы выполнить очередное действие над объектом
            repeat(ticks) { i ->
                logger.debug("before tick $i")
                delay(GAME_ACTION_PERIOD)
                logger.debug("after tick $i")
            }

            logger.debug("run action")
            // отправим на исполнение кусок кода который должен выполнить объект
            val targetResult = CompletableDeferred<Boolean>()
            target.send(GameObjectMsg.ExecuteActionTick(this@Action, targetResult, block))
            // если объект говорит что действие завершилось
            // заканчиваем цикл повтора действий
            if (targetResult.await()) {
                me.send(PlayerMsg.StopAction())
                break
            }
        }
    }

    suspend fun stop() {
        logger.debug("stop action")
        job.cancelAndJoin()
        logger.debug("action was stopped")

        sendPkt(ActionProgress(-1, -1))
    }

    suspend fun sendPkt(m: ServerMessage) {
        if (me is Player) me.session.send(m)
    }
}