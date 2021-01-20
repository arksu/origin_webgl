package com.origin.model

import com.origin.ServerConfig
import com.origin.net.model.ObjectMoved
import com.origin.net.model.ObjectStartMove
import com.origin.net.model.ObjectStopped
import com.origin.utils.ObjectID
import com.origin.utils.TILE_SIZE
import com.origin.utils.Vec2i
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * гуманоид
 * обладает зрением (видимые объекты о которых "знает")
 */
@ObsoleteCoroutinesApi
abstract class Human(id: ObjectID, x: Int, y: Int, level: Int, region: Int, heading: Short) :
    MovingObject(id, x, y, level, region, heading) {

    /**
     * дистанция на которой мы видим объекты
     * может изменяться динамически (ночью видим хуже)
     */
    private var visibleDistance = 15 * TILE_SIZE

    /**
     * объекты которые известны мне, инфа о которых отправляется и синхронизирована с клиентом
     * любое добавление в этот список, а равно как и удаление из него должно быть
     * синхронизировано с клиентом
     */
    protected val knownList by lazy { KnownList(this) }

    /**
     * последняя позиция в которой было обновление видимых объектов
     * нужно чтобы часто не обновлять список видимых (слишком накладно)
     */
    private var lastPosUpdateVisible: Vec2i? = null

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
            else -> super.processMessage(msg)
        }
    }

    override suspend fun afterSpawn() {
        super.afterSpawn()
        updateVisibleObjects(true)
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
     * вижу ли я указаный объект
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
        if (force || (lastPosUpdateVisible != null
                    && pos.point != lastPosUpdateVisible
                    && pos.point.dist(lastPosUpdateVisible!!) > ServerConfig.VISIBLE_UPDATE_DISTANCE)
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
            logger.warn("updateVisibleObjects $this total vis=${knownList.size()} new=$newCounter del=${delCounter}")
        }
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
}