package com.origin.model.action

import com.origin.TimeController.GAME_ACTION_PERIOD
import com.origin.model.Human
import com.origin.model.HumanMessage
import com.origin.model.Player
import com.origin.net.ActionProgress
import com.origin.util.WorkerScope
import kotlinx.coroutines.*
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
    protected val me: Human,
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
    abstract val minimumStaminaRequired: Int

    open val tickDelay = GAME_ACTION_PERIOD

    var tick: Int = 0

    /**
     * условие для начала действия или его продолжения
     */
    open fun condition(): Boolean {
        return if (minimumStaminaRequired == 0) {
            true
        } else if (me.status.stamina < minimumStaminaRequired) {
            if (me is Player) runBlocking {
                me.systemSay("Not enough stamina for this action")
            }
            return false
        } else {
            true
        }
    }

    /**
     * при создании действия запускается корутина
     */
    private val job: Job = WorkerScope.launch {
        // если вообще не можем запустить действие сразу выходим отсюда, на клиент даже ничего не шлем
        if (!condition()) {
            logger.debug("cant start Action. condition check failed")
            me.send(HumanMessage.StopAction())
            return@launch
        }

        // в самом начале отправим прогресс на клиент, чтобы отобразить его визуально
        sendProgress()

        // повторяем циклы задержки и выполнения блока
        do {
            // ждем нужное количество тиков, чтобы выполнить очередное действие/цикл над объектом
            repeat(ticks) { i ->
//                logger.debug("delay tick $i...")
                delay(tickDelay)
                tick++
                if (tick < ticks) sendProgress()
            }

            // если не можем поглотить стамину на очередной цикл действия - выходим из цикла
            if (staminaConsume > 0 && !me.status.checkAndReduceStamina(staminaConsume)) break

            logger.debug("action run...")
            val isFinished = run()
            if (isFinished) break
            tick = 0
            sendProgress()
        } while (condition())

        // корректно завершим действие
        me.send(HumanMessage.StopAction())
    }

    /**
     * кастомно вычисляемый прогресс действия
     */
    open fun getProgress(): Pair<Int, Int>? {
        return null
    }

    private suspend fun sendProgress() {
        if (me is Player) {
            val p = getProgress()
            if (p != null) {
                logger.debug("progress ${p.first} ${p.second}")
                me.sendToSocket(ActionProgress(p.first, p.second))
            } else {
                logger.debug("progress $tick ${ticks - 1}")
                // чтобы прогресс бар в конце доходил до конца надо послать на 1 тик меньше.
                me.sendToSocket(ActionProgress(tick, ticks - 1))
            }
        }
    }

    /**
     * выполнить основную логику очередного цикла действия
     * @return false - если действие надо продолжать (isFinished = false)
     */
    abstract suspend fun run(): Boolean

    suspend fun stop() {
        logger.debug("stop action...")
        job.cancelAndJoin()
        logger.debug("action was stopped")

        if (me is Player) me.sendToSocket(ActionProgress(-1, -1))
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Action::class.java)
    }
}