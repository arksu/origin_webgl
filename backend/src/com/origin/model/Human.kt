package com.origin.model

import com.origin.ServerConfig
import com.origin.model.move.MoveController
import com.origin.net.model.ObjectAdd
import com.origin.net.model.ObjectMoved
import com.origin.net.model.ObjectStartMove
import com.origin.net.model.ObjectStopped
import com.origin.utils.ObjectID
import com.origin.utils.TILE_SIZE
import com.origin.utils.Vec2i
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

sealed class HumanMSg {
    class StopAction
    class StatusRegeneration
}

/**
 * гуманоид
 * обладает зрением (видимые объекты о которых "знает")
 */
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
abstract class Human(id: ObjectID, x: Int, y: Int, level: Int, region: Int, heading: Short) :
    MovingObject(id, x, y, level, region, heading) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Human::class.java)
    }

    /**
     * дистанция на которой мы видим объекты
     * может изменяться динамически (ночью видим хуже)
     */
    private var visibleDistance = 60 * TILE_SIZE

    /**
     * объекты которые известны мне, инфа о которых отправляется и синхронизирована с клиентом
     * любое добавление в этот список, а равно как и удаление из него должно быть
     * синхронизировано с клиентом
     */
    protected val knownList by lazy { KnownList(this) }

    /**
     * список объектов которые "открыли" (есть инвентарь и надо его отобразить на клиенте)
     */
    protected val openObjectsList by lazy { OpenObjectsList(this) }

    /**
     * последняя позиция в которой было обновление видимых объектов
     * нужно чтобы часто не обновлять список видимых (слишком накладно)
     */
    private var lastPosUpdateVisible: Vec2i? = null

    /**
     * действие которое выполняет объект в данный момент
     */
    var action: Action? = null

    abstract val status: Status

    /**
     * этот чувак мертв?
     */
    var isDead = false
        private set

    /**
     * в нокауте?
     */
    var isKnocked = false
        private set

    override suspend fun processMessage(msg: Any) {
        when (msg) {
            is BroadcastEvent.StartMove -> {
                if (knownList.isKnownObject(msg.obj)) {
                    if (this is Player) session.send(ObjectStartMove(msg))
                }
            }
            is BroadcastEvent.Moved -> {
                // если мы знаем объект
                if (knownList.isKnownObject(msg.obj)) {
                    // больше его не видим
                    if (!isObjectVisibleForMe(msg.obj)) {
                        // удаляем объект из видимых
                        knownList.removeKnownObject(msg.obj)
                    } else {
                        if (this is Player) session.send(ObjectMoved(msg))
                    }
                } else {
                    // объект не знаем. но видим
                    if (isObjectVisibleForMe(msg.obj)) {
                        knownList.addKnownObject(msg.obj)
                        if (this is Player) session.send(ObjectMoved(msg))
                    }
                }
            }
            is BroadcastEvent.Stopped -> {
                // если мы знаем объект
                if (knownList.isKnownObject(msg.obj)) {
                    // больше его не видим
                    if (!isObjectVisibleForMe(msg.obj)) {
                        // удаляем объект из видимых
                        knownList.removeKnownObject(msg.obj)
                    } else {
                        if (this is Player) session.send(ObjectStopped(msg))
                    }
                } else {
                    // объект не знаем. но видим
                    if (isObjectVisibleForMe(msg.obj)) {
                        knownList.addKnownObject(msg.obj)
                        if (this is Player) session.send(ObjectStopped(msg))
                    }
                }
            }
            is BroadcastEvent.Changed -> {
                if (knownList.isKnownObject(msg.obj)) {
                    if (this is Player) session.send(ObjectAdd(msg.obj))
                }
            }
            is HumanMSg.StopAction -> stopAction()
            is HumanMSg.StatusRegeneration -> status.regeneration()

            else -> super.processMessage(msg)
        }
    }

    override suspend fun afterSpawn() {
        super.afterSpawn()
        status.startRegeneration()
        updateVisibleObjects(true)
        broadcastStatusUpdate()
    }

    /**
     * когда ЭТОТ объект удален из грида
     */
    override suspend fun onRemoved() {
        super.onRemoved()
        knownList.clear()
    }

    /**
     * добавили объект в грид в котором находится объект
     */
    override suspend fun onObjectAdded(obj: GameObject) {
        super.onObjectAdded(obj)
        if (isObjectVisibleForMe(obj)) {
            knownList.addKnownObject(obj)
        }
    }

    /**
     * грид говорит что какой то объект был удален
     */
    override suspend fun onObjectRemoved(obj: GameObject) {
        super.onObjectRemoved(obj)
        logger.debug("human onObjectRemoved $obj")
        knownList.removeKnownObject(obj)
    }

    /**
     * Вижу ли я указанный объект
     */
    private fun isObjectVisibleForMe(obj: GameObject): Boolean {
        // себя всегда видим!
        return obj.id == this.id || pos.dist(obj.pos) < visibleDistance
    }

    /**
     * обновить список видимых объектов
     * все новые что увидим - отправятся клиенту. старые что перестали видеть - будут удалены
     * @param force принудительно, иначе проверка будет только если отошли на значительное расстояние от точки последней проверки
     */
    suspend fun updateVisibleObjects(force: Boolean) {
        if (force || (
                    lastPosUpdateVisible != null &&
                            pos.point != lastPosUpdateVisible &&
                            pos.point.dist(lastPosUpdateVisible!!) > ServerConfig.VISIBLE_UPDATE_DISTANCE
                    )
        ) {
            var newCounter = 0
            var delCounter = 0
            // проходим по всем гридам в которых находимся
            for (grid in grids) {
                // по всем объектам в гридах
                for (o in grid.objects) {
                    // если объект реально видим для меня
                    if (isObjectVisibleForMe(o)) {
                        if (knownList.addKnownObject(o)) newCounter++
                    } else {
                        if (knownList.removeKnownObject(o)) delCounter++
                    }
                }
            }

            lastPosUpdateVisible = pos.point.copy()
            logger.warn("updateVisibleObjects $this total vis=${knownList.size()} new=$newCounter del=$delCounter")
        }
    }

    override suspend fun startMove(controller: MoveController) {
        openObjectsList.closeAll()
        super.startMove(controller)
        stopAction()
    }

    override suspend fun stopMove() {
        super.stopMove()
        updateVisibleObjects(false)
    }

    override suspend fun onEnterGrid(grid: Grid) {
        super.onEnterGrid(grid)
        logger.warn("Activate ${grid.x} ${grid.y} pos=$pos")
        grid.sendJob(GridMsg.Activate(this, Job())).join()
    }

    override suspend fun onLeaveGrid(grid: Grid) {
        super.onLeaveGrid(grid)
        logger.warn("Deactivate ${grid.x} ${grid.y}")
        grid.sendJob(GridMsg.Deactivate(this, Job())).join()
    }

    /**
     * запустить однократное действие
     * каждый шаг (step) будет выполняться условие на игроке playerCondition
     */
    fun startOnceAction(
        target: GameObject,
        ticksPerStep: Int,
        stepsCount: Int,
        playerCondition: ((Human) -> Boolean)? = null,
        block: suspend (Action) -> Boolean,
    ) {
        action = Action(this, target, ticksPerStep, false, 0, stepsCount, playerCondition, block)
    }

    /**
     * запустить цикличное действие
     * перед каждым циклом выполнится playerCondition (снять стамину, хп и тд)
     * после завершения тиков на цикл (cycleLengthTicks) выполняется блок до тех пор, пока не вернет true
     */
    fun startCyclicAction(
        target: GameObject,
        ticksPerCycle: Int,
        startProgress: Int,
        totalProgress: Int,
        playerCondition: ((Human) -> Boolean)? = null,
        block: suspend (Action) -> Boolean
    ) {
        action = Action(this, target, ticksPerCycle, true, startProgress, totalProgress, playerCondition, block)
    }

    suspend fun stopAction() {
        action?.stop()
        action = null
    }

    abstract fun getMaxSoftHp(): Double

    abstract fun getMaxStamina(): Double

    /**
     * вырубить в нокаут на 1 минуту
     */
    fun doKnock() {
        isKnocked = true
        broadcastStatusUpdate()
        status.stopRegeneration()
        // TODO timer of 1 min
    }

    /**
     * существо умирает
     */
    fun doDie() {
        isDead = true
        broadcastStatusUpdate()
        status.stopRegeneration()
        // TODO die
    }

    abstract fun broadcastStatusUpdate()
}
