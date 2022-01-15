package com.origin.model

import com.origin.TimeController.GAME_ACTION_PERIOD
import com.origin.entity.InventoryItemEntity
import com.origin.model.inventory.InventoryItem
import com.origin.model.inventory.ItemType
import com.origin.net.model.ActionProgress
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
class Action(
    private val me: Human,
    val target: GameObject,
    /**
     * сколько тиков между действиями (берем по модулю, не зависимо от знака)
     */
    private val ticks: Int,
    /**
     * действие циклично? выполняем до тех пор пока actionResultBlock не вернет true
     */
    private val isCyclic: Boolean,
    private val startProgress: Int,
    val maxProgress: Int,
    private val playerCondition: ((Human) -> Boolean)?,
    private val actionResultBlock: suspend (Action, GameObject) -> Boolean,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Action::class.java)

        fun generateItems(vararg type: ItemType): suspend (Action, GameObject) -> Boolean {
            return { _, gameObject ->
                type.forEach { itemType ->
                    val newItem = transaction {
                        val e = InventoryItemEntity.makeNew(itemType)
                        InventoryItem(e, null)
                    }
                    val result = CompletableDeferred<Boolean>()
                    gameObject.send(GameObjectMsg.PutItem(newItem, -1, -1, result))
                    result.await()
                }
                true
            }
        }
    }

    /**
     * текущий прогресс. увеличиваем с каждым выполненным периодом
     * служит только для служебных целей, для действий которые не цикличны
     */
    private var currentProgress = startProgress

    /**
     * при создании действия запускается корутина
     */
    private val job: Job = WorkerScope.launch {
        sendPacket(ActionProgress(startProgress, maxProgress))
        logger.warn("start new action")

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

            // ждем нужное количество тиков чтобы выполнить очередное действие/цикл над объектом
            repeat(ticks) { i ->
                logger.debug("delay tick $i...")
                delay(GAME_ACTION_PERIOD)
            }
            if (!isCyclic) {
                currentProgress++
                logger.debug("after delay currentProgress=$currentProgress")
                sendCurrentProgress()
                // действие завершено полностью?
                if (currentProgress >= maxProgress && executeResultAction()) {
                    break
                }
            } else {
                if (executeResultAction()) break
            }
        }
    }

    private suspend fun executeResultAction(): Boolean {
        logger.debug("run action")
        // отправим на исполнение кусок кода который должен выполнить объект
        val targetResult = CompletableDeferred<Boolean>()
        target.send(GameObjectMsg.ExecuteActionTick(this@Action, targetResult, actionResultBlock))
        // если объект говорит что действие завершилось
        // заканчиваем цикл повтора действий
        if (targetResult.await()) {
            me.send(HumanMSg.StopAction())
            return true
        }
        return false
    }

    suspend fun stop() {
        logger.debug("stop action...")
        job.cancelAndJoin()
        logger.debug("action was stopped")

        sendPacket(ActionProgress(-1, -1))
    }

    suspend fun sendPacket(m: ActionProgress) {
        if (me is Player) me.session.send(m)
    }

    private suspend fun sendCurrentProgress() {
        sendPacket(ActionProgress(currentProgress, maxProgress))
    }
}
