package com.origin

import com.origin.model.Grid
import com.origin.model.message.GridMessage
import com.origin.model.message.MovingObjectMessage
import com.origin.model.World
import com.origin.move.MovingObject
import com.origin.util.WorkerScope
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object TimeController : Thread("TimeController") {
    val logger: Logger = LoggerFactory.getLogger(TimeController::class.java)

    init {
        super.setDaemon(true)
        super.setPriority(MAX_PRIORITY)
        load()
    }

    private const val KEY_GAME_TIME = "gameTime"

    /**
     * игровых тиков в реальной секунде (на передвижение)
     * должно делится на цело 1000 / TICKS_PER_SECOND
     * 4, 5, 8, 10
     */
    private const val TICKS_PER_SECOND = 5

    /**
     * период обновления гридов и объектов в них (в тиках)
     */
    const val GRID_UPDATE_PERIOD = 12 * TICKS_PER_SECOND

    /**
     * сколько мсек длится минимальный игровой тик (между итерациями движения)
     */
    private const val MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND

    /**
     * сколько реальных часов длится игровой день
     */
    private const val GAME_DAY_IN_REAL_HOURS = 1

    /**
     * сколько игровых дней в одном игровом месяце (нужно для фаз луны и прочего)
     */
    private const val GAME_DAYS_IN_MONTH = 30

    /**
     * сколько игровых месяцев в одноум году
     */
    private const val GAME_MONTH_IN_YEAR = 12

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
    private const val TICKS_IN_GAME_DAY = SECONDS_IN_GAME_DAY * TICKS_PER_SECOND

    /**
     * тиков в одной игровой минуте
     */
    private const val TICKS_IN_GAME_MINUTE = TICKS_IN_GAME_DAY / 1440 // 24 * 60

    /**
     * сколько длится тик для игрового действия
     */
    const val GAME_ACTION_PERIOD = 100L

    /**
     * сколько занимает тик регенерации для игроков (мсек)
     */
    const val PLAYER_REGENERATION_PERIOD = 800

    /**
     * время между принудительным сохранением состояния игрока
     */
    const val PLAYER_SAVE_PERIOD = 10000

    /**
     * как часто шлем обновление игрового времени на клиент
     */
    const val PLAYER_TIME_UPDATE_PERIOD = 3000

    /**
     * период в тиках между сохранением значения времени в базу
     */
    private const val STORE_TICKS_PERIOD = 5 * TICKS_PER_SECOND

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
    @Volatile
    var tickCount: Long = 0
        private set

    private var playersSaveTimeAccum = 0

    private var playersRegenerateTimeAccum = 0

    private var playersTimeUpdateAccum = 0

    /**
     * загрузка информации о времени из базы
     */
    private fun load() {
        logger.debug("TimeController load...")
        // ко времени полученному из базы добавим период
        // т.к. объекты могли быть уже обновлены и новое время у них сохранено. а мы можем получить старое время из базы
        // поэтому ставим заведомо большее игровое время
        // чтобы исключить ситуации когда в контроллере время отстанет от времени в игровых объектах
        tickCount = GlobalVariables.getLong(KEY_GAME_TIME) + STORE_TICKS_PERIOD
    }

    /**
     * сохранить информацию об игровом времени в базу
     */
    private fun store() {
        GlobalVariables.saveLong(KEY_GAME_TIME, tickCount)
    }

    fun addTicks(value: Long) {
        tickCount += value
    }

    /**
     * текущее игровое время суток в игровых минутах (минут от начала дня)
     */
    private fun getGameTime(): Int {
        return ((tickCount % TICKS_IN_GAME_DAY) / TICKS_IN_GAME_MINUTE).toInt()
    }

    /**
     * сколько тиков апдейтов гридов прошло с начала мира
     */
    private fun getGridTicks(): Long {
        return (tickCount / GRID_UPDATE_PERIOD)
    }

    /**
     * номер текущего игрового дня в месяце
     */
    fun getGameDay(): Int {
        return ((tickCount / TICKS_IN_GAME_DAY) % GAME_DAYS_IN_MONTH).toInt()
    }

    /**
     * номер игрового месяца
     */
    fun getGameMonth(): Int {
        return (tickCount / (TICKS_IN_GAME_DAY * GAME_DAYS_IN_MONTH)).toInt()
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

    /**
     * на сколько сильно "наступила" ночь (0-255)
     */
    fun getNightValue(): Int {
        val h = getGameHour()
        val m = getGameMinute()
        return when (h) {
            // рассвет
            5 -> {
                return (((60 - m).toFloat() / 60f) * 255f).toInt()
            }

            in 6..20 -> {
                return 0
            }
            // закат
            21 -> {
                return ((m.toFloat() / 60f) * 255f).toInt()
            }

            else -> 255
        }
    }

    /**
     * угол подъема солнца. от восхода к закату. 0-255, 127 - зенит
     */
    fun getSunValue(): Int {
        return when (val h = getGameHour()) {
            in 6..21 -> (((h - 5).toFloat() / 16f) * 255f).toInt()
            else -> 0
        }
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
                    it.send(MovingObjectMessage.UpdateMove())
                }
            } catch (e: ClosedSendChannelException) {
                logger.warn("ClosedSendChannelException")
                // актор у объекта был убит, а нас не уведомили. исправим ситуацию
                movingObjects.remove(it)
            }
        }
    }

    private fun updateGrids() {
        runBlocking {
            activeGrids.forEach {
                kotlin.runCatching {
                    it.send(GridMessage.Update())
                }
            }
        }
    }

    override fun run() {
        logger.debug("TimeController started")
        var lastStoreTick = tickCount
        var lastGridTick = getGridTicks()

        while (active) {
            val nextTickTime = (System.currentTimeMillis() / MILLIS_IN_TICK) * MILLIS_IN_TICK + MILLIS_IN_TICK
            tickCount++

            try {
                moveObjects()

                // обновляем все гриды
                if (getGridTicks() > lastGridTick) {
                    updateGrids()
                    lastGridTick = getGridTicks()
                }

                // сохраняем всех игроков если надо
                playersSaveTimeAccum += MILLIS_IN_TICK
                if (playersSaveTimeAccum >= PLAYER_SAVE_PERIOD) {
                    playersSaveTimeAccum -= PLAYER_SAVE_PERIOD
                    if (World.getPlayersCount() > 0) WorkerScope.launch {
                        World.playersIterator().forEach { pe ->
                            pe.value.save()
                        }
                    }
                }

                // регенерация игроков
                playersRegenerateTimeAccum += MILLIS_IN_TICK
                while (playersRegenerateTimeAccum >= PLAYER_REGENERATION_PERIOD) {
                    playersRegenerateTimeAccum -= PLAYER_REGENERATION_PERIOD
                    if (World.getPlayersCount() > 0) WorkerScope.launch {
                        World.playersIterator().forEach { pe ->
                            pe.value.updateRegeneration()
                        }
                    }
                }

                // обновление времени на клиенте
                playersTimeUpdateAccum += MILLIS_IN_TICK
                if (playersTimeUpdateAccum >= PLAYER_TIME_UPDATE_PERIOD) {
                    playersTimeUpdateAccum -= PLAYER_TIME_UPDATE_PERIOD
                    if (World.getPlayersCount() > 0) WorkerScope.launch {
                        World.playersIterator().forEach { pe ->
                            pe.value.sendTimeUpdate()
                        }
                    }
                }
            } catch (t: Throwable) {
                logger.error("TimeController update error ${t.message}", t)
                Shutdown.start()
            }

            if (tickCount - lastStoreTick > STORE_TICKS_PERIOD) {
                // запустим сохранение времени в базу в фоне (в корутине)
                WorkerScope.launch {
//                    logger.debug("time tick=$tickCount real hours=${(tickCount / (TICKS_PER_SECOND * 3600))} game day=${tickCount / TICKS_IN_GAME_DAY} time=${getGameHour()}:${getGameMinute()}")
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
