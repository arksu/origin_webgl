package com.origin

import com.origin.entity.GlobalVariables
import com.origin.model.Grid
import com.origin.model.GridMsg
import com.origin.model.MovingObject
import com.origin.model.MovingObjectMsg
import com.origin.utils.WorkerScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

@ObsoleteCoroutinesApi
class TimeController : Thread("TimeController") {
    init {
        super.setDaemon(true)
        super.setPriority(MAX_PRIORITY)
        load()
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(TimeController::class.java)
        val instance = TimeController()

        private const val KEY = "gameTime"

        /**
         * игровых тиков в реальной секунде (на передвижение)
         */
        const val TICKS_PER_SECOND = 2

        /**
         * период обновления гридов и объектов в них (в тиках)
         */
        const val GRID_UPDATE_PERIOD = 10 * TICKS_PER_SECOND

        /**
         * сколько мсек длится минимальный игровой тик (между итерациями движения)
         */
        const val MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND

        /**
         * сколько реальных часов длится игровой день
         */
        private const val GAME_DAY_IN_REAL_HOURS = 8

        /**
         * сколько реальных секунд в игровом дне
         */
        private const val SECONDS_IN_GAME_DAY = GAME_DAY_IN_REAL_HOURS * 3600

        /**
         * сколько реальных мсек в игровом дне
         */
//        const val MILLIS_IN_GAME_DAY = SECONDS_IN_GAME_DAY * 1000

        /**
         * сколько тиков в одном игровом дне
         */
        const val TICKS_IN_GAME_DAY = SECONDS_IN_GAME_DAY * TICKS_PER_SECOND

        /**
         * тиков в одной игровой минуте
         */
        const val TICKS_IN_GAME_MINUTE = TICKS_IN_GAME_DAY / 1440 // 24 * 60

        /**
         * сколько длится тик для игрового действия
         */
        const val GAME_ACTION_PERIOD = 500

        /**
         * период в тиках между сохранением значения времени в базу
         */
        private const val STORE_TICKS_PERIOD = 5 * TICKS_PER_SECOND
    }

    /**
     * список объектов которые движуться в данный момент
     */
    private val movingObjects = ConcurrentHashMap.newKeySet<MovingObject>()

    /**
     * список активных гридов которые надо обновлять
     */
    private val activeGrids = ConcurrentHashMap.newKeySet<Grid>(9)

    /**
     * активен ли еще мир (выключаем на shutdown server)
     */
    @Volatile
    private var active = true

    /**
     * игровое время в тиках (сколько тиков с начала мира прошло)
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
        GlobalVariables.saveLong(KEY, tickCount)
    }

    /**
     * текущее игровое время суток в игровых минутах (минут от начала дня)
     */
    fun getGameTime(): Int {
        return ((tickCount % TICKS_IN_GAME_DAY) / TICKS_IN_GAME_MINUTE).toInt()
    }

    /**
     * сколько тиков апдейтов гридов прошло с начала мира
     */
    private fun getGridTicks(): Int {
        return (tickCount / GRID_UPDATE_PERIOD).toInt()
    }

    /**
     * часов от начала игрового дня
     */
    fun getGameHour(): Int {
        return getGameTime() / 60
    }

    /**
     * минут в текущем часе
     */
    fun getGameMinute(): Int {
        return getGameTime() % 60
    }

    fun addMovingObject(obj: MovingObject) {
        movingObjects.add(obj)
    }

    fun deleteMovingObject(obj: MovingObject) {
        movingObjects.remove(obj)
    }

    /**
     * добавить активный грид в список активных (для апдейта)
     */
    fun addActiveGrid(grid: Grid) {
        activeGrids.add(grid)
    }

    /**
     * удалить активный грид (больше не будет обновляться)
     */
    fun removeActiveGrid(grid: Grid) {
        activeGrids.remove(grid)
    }

    fun shutdown() {
        super.interrupt()
        active = false
    }

    private fun moveObjects() {
        if (movingObjects.size > 0) movingObjects.forEach {
            try {
                runBlocking {
                    it.send(MovingObjectMsg.UpdateMove())
                }
            } catch (e: ClosedSendChannelException) {
                logger.warn("ClosedSendChannelException")
                // актор у объекта был убит, а нас не уведомили. исправим ситуацию
                movingObjects.remove(it)
            }
        }
    }

    private fun updateGrids() {
        activeGrids.forEach {
            runBlocking {
                it.send(GridMsg.Update())
            }
        }
    }

    override fun run() {
        var lastStoreTick = tickCount
        var lastGridTick = getGridTicks()

        while (active) {
            val nextTickTime = (System.currentTimeMillis() / MILLIS_IN_TICK) * MILLIS_IN_TICK + MILLIS_IN_TICK
            tickCount++

            try {
                moveObjects()
                if (getGridTicks() > lastGridTick) {
                    updateGrids()
                    lastGridTick = getGridTicks()
                }
            } catch (t: Throwable) {
                logger.error("TimeController update error ${t.message}", t)
                Shutdown.start()
            }

            if (tickCount - lastStoreTick > STORE_TICKS_PERIOD) {
                // запустим сохранение времени в базу в фоне (в корутине)
                WorkerScope.launch {
                    logger.debug("time tick=$tickCount real hours=${(tickCount / (TICKS_PER_SECOND * 3600))} game day=${tickCount / TICKS_IN_GAME_DAY} time=${getGameHour()}:${getGameMinute()}")
                    store()
                }
                lastStoreTick = tickCount
            }

            val sleepTime = nextTickTime - System.currentTimeMillis()
            if (sleepTime > 0) {
                try {
                    sleep(sleepTime)
                } catch (e: InterruptedException) {
                    active = false
                }
            }
        }
    }
}