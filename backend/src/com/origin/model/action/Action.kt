package com.origin.model.action

import com.origin.TimeController.GAME_ACTION_PERIOD
import com.origin.model.Human
import com.origin.model.HumanMessage
import com.origin.model.Player
import com.origin.net.ActionProgress
import com.origin.util.WorkerScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * выполнение игровых действий
 *
 * рубка дерева:
 *  каждое действие отнимает N hp у дерева, N - определяется расчетом
 *  если hp кончилось - следующего действия нет, выполняем завершающий блок кода
 *
 * взять ветку
 *  каждое действие проверяет можно ли взять ветку с дерева. есть ли там еще
 *  если ветки еще есть - спавним в инвентарь ветку
 *  если веток нет - продолжать не даем. завершаем
 */
abstract class Action(
    val me: Human,
) {
    /**
     * сколько тиков занимает один цикл действия
     */
    abstract val ticks: Int

    /**
     * сколько расходуем стамины за 1 цикл
     */
    abstract val staminaConsume: Int

    /**
     * минимальное количество стамины которое требуется, чтобы разрешить совершать данное действие
     */
    abstract val staminaRequired: Int

    /**
     * условие для начала действия или его продолжения
     */
    open fun condition(): Boolean {
        return staminaRequired == 0 || staminaRequired >= me.status.stamina
    }

    /**
     * при создании действия запускается корутина
     */
    private val job: Job = WorkerScope.launch {
        sendProgress()
        // повторяем циклы задержки и выполнения блока
        while (condition()) {
            if (staminaConsume > 0 && !me.status.checkAndReduceStamina(staminaConsume)) break

            // ждем нужное количество тиков, чтобы выполнить очередное действие/цикл над объектом
            repeat(ticks) { i ->
                logger.debug("delay tick $i...")
                delay(GAME_ACTION_PERIOD)
            }

            val needContinue = run()
            if (!needContinue) break
        }
        me.send(HumanMessage.StopAction())
    }

    private suspend fun sendProgress() {

    }

    abstract fun run(): Boolean

    suspend fun stop() {
        logger.debug("stop action...")
        job.cancelAndJoin()
        logger.debug("action was stopped")

        if (me is Player) me.session.send(ActionProgress(-1, -1))
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Action::class.java)
    }
}