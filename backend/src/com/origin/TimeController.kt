package com.origin

import com.origin.entity.GlobalVariables
import com.origin.model.MovingObject
import com.origin.model.MovingObjectMsg
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap


@ObsoleteCoroutinesApi
class TimeController : Thread("TimeController") {
    init {
        super.setDaemon(true)
        super.setPriority(MAX_PRIORITY)
        load()
    }

    companion object {
        val instance = TimeController()

        private const val KEY = "gameTime"

        /**
         * игровых тиков в реальной секунде (на передвижение)
         */
        const val TICKS_PER_SECOND = 5

        /**
         * сколько мсек длится минимальный игровой тик (между итерациями движения)
         */
        const val MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND

        /**
         * сколько реальных часов длится игровой день
         */
        const val GAME_DAY_IN_REAL_HOURS = 4;

        /**
         * сколько реальных секунд в игровом дне
         */
        const val SECONDS_IN_GAME_DAY = GAME_DAY_IN_REAL_HOURS * 3600

        /**
         * сколько реальных мсек в игровом дне
         */
        const val MILLIS_IN_GAME_DAY = SECONDS_IN_GAME_DAY * 1000

        /**
         * сколько длится тик для игрового действия
         */
        const val GAME_ACTION_PERIOD = 500

        /**
         * период в тиках между сохранением значения времени в базу
         */
        private const val STORE_TICKS_PERIOD = TICKS_PER_SECOND * 5;
    }

    /**
     * список объектов которые движуться в данный момент
     */
    private val movingObjects = ConcurrentHashMap.newKeySet<MovingObject>()

    /**
     * активен ли еще мир (выключаем на shutdown server)
     */
    @Volatile
    private var active = true

    /**
     * игровое время в тиках
     * @see TICKS_PER_SECOND
     */
    private var tickCount: Long = 0

    /**
     * загрузка информации о времени из базы
     */
    private fun load() {
        // ко времени полученному из базы добавим период
        // т.к. объекты могли быть уже обновлены и новое время у них сохранено. а мы можем получить старое время из базы
        // поэтому ставим заведомо большее игровое время
        // чтобы исключить ситуации когда в контроллере время отстанет от времени в игровых объектах
        tickCount = GlobalVariables.getLong(KEY) + STORE_TICKS_PERIOD
    }

    /**
     * сохранить информацию об игровом времени в базу
     */
    private fun store() {
        println("store")
        GlobalVariables.saveLong(KEY, tickCount)
    }

    fun addMovingObject(obj: MovingObject) {
        movingObjects.add(obj)
    }

    fun deleteMovingObject(obj: MovingObject) {
        movingObjects.remove(obj)
    }

    fun shutdown() {
        super.interrupt()
        active = false
    }

    private fun moveObjects() {
        if (movingObjects.size > 0) movingObjects.forEach {
            try {
                runBlocking {
                    it.send(MovingObjectMsg.MoveUpdate())
                }
            } catch (e: ClosedSendChannelException) {
                logger.warn("ClosedSendChannelException")
                // актор у объекта был убит, а нас не уведомили. исправим ситуацию
                movingObjects.remove(it)
            } catch (t: Throwable) {
                logger.error("send move update error ${t.message}", t)
            }
        }
    }

    override fun run() {
        var lastStoreTick = tickCount
        while (active) {
            val nextTickTime = (System.currentTimeMillis() / MILLIS_IN_TICK) * MILLIS_IN_TICK + MILLIS_IN_TICK
            tickCount++

            try {
                moveObjects()
            } catch (t: Throwable) {

            }

            if (tickCount - lastStoreTick > STORE_TICKS_PERIOD) {
                store()
                lastStoreTick = tickCount
            }

            val sleepTime = nextTickTime - System.currentTimeMillis()
            if (sleepTime > 0) {
                logger.debug("time to sleep $sleepTime")
                try {
                    sleep(sleepTime)
                } catch (e: InterruptedException) {
                    active = false
                }
            }
        }
    }
}