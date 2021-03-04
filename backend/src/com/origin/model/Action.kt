package com.origin.model

import com.origin.TimeController.GAME_ACTION_PERIOD
import com.origin.net.model.ActionProgress
import com.origin.net.model.ServerMessage
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.abs

/**
 * выполнение игровых действий
 * действия выполняются циклично.
 * 1. проверяем выполнение условия для начала действия playerCondition
 * 2. засыпаем на ticks тиков. каждый тик длится GAME_ACTION_PERIOD
 * 3. затем вызывается actionBlock
 * 4. если actionBlock вернул ложь значит цикл действия не закончен. над объектом можно продолжать работу
 * все выполняется еще раз с пункта 1.
 *
 * если ticks < 0
 * значит это действие НЕ циклично. выполним проверку playerCondition только 1 раз в самом начале
 * затем выполняем maxProgress повторений цикла ticks тиков
 * между каждым циклом шлем sendCurrentProgress
 */
@ObsoleteCoroutinesApi
class Action(
    private val me: Human,
    val target: GameObject,
    /**
     * скольок тиков между действиями
     */
    private val ticks: Int,
    private val startProgress: Int,
    val maxProgress: Int,
    private val playerCondition: ((Player) -> Boolean)?,
    private val actionBlock: suspend (Action) -> Boolean,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Action::class.java)
    }

    /**
     * текущий прогресс. увеличиваем с каждым выполненным периодом
     * служит только для служебных целей, для действий которые не цикличны
     */
    var currentProgress = startProgress
        private set

    /**
     * при создании действия запускается корутина
     */
    private val job: Job = WorkerScope.launch {
        sendPkt(ActionProgress(startProgress, maxProgress))
        logger.warn("start new action")

        if (ticks < 0) {
            if (playerCondition != null) {
                // ДО каждого тика проверяем выполнение условий на игроке (стамина, голод и тд)
                val playerResult = CompletableDeferred<Boolean>()
                me.send(PlayerMsg.ExecuteActionCondition(playerResult, playerCondition))
                // условие на игроке вернуло ложь. значит не выполнилось условие для следующего тика действия
                playerResult.await()
            }
        }

        // повтоярем циклы задержки и выполнения блока
        while (true) {
            if (ticks > 0 && playerCondition != null) {
                // ДО каждого тика проверяем выполнение условий на игроке (стамина, голод и тд)
                val playerResult = CompletableDeferred<Boolean>()
                me.send(PlayerMsg.ExecuteActionCondition(playerResult, playerCondition))
                // условие на игроке вернуло ложь. значит не выполнилось условие для следующего тика действия
                if (!playerResult.await()) {
                    break
                }
            }

            // ждем нужное количество тиков чтобы выполнить очередное действие над объектом
            repeat(abs(ticks)) { i ->
                logger.debug("before tick $i")
                delay(GAME_ACTION_PERIOD)
                logger.debug("after tick $i currentProgress=$currentProgress")
            }
            currentProgress++
            if (ticks < 0) {
                sendCurrentProgress()
                if (currentProgress >= maxProgress && runAction()) {
                    break
                }
            } else {
                if (runAction()) break
            }
        }
    }

    private suspend fun runAction(): Boolean {
        logger.debug("run action")
        // отправим на исполнение кусок кода который должен выполнить объект
        val targetResult = CompletableDeferred<Boolean>()
        target.send(GameObjectMsg.ExecuteActionTick(this@Action, targetResult, actionBlock))
        // если объект говорит что действие завершилось
        // заканчиваем цикл повтора действий
        if (targetResult.await()) {
            me.send(HumanMSg.StopAction())
            return true
        }
        return false
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

    suspend fun sendCurrentProgress() {
        sendPkt(ActionProgress(currentProgress, maxProgress))
    }
}