package com.origin.model

import com.origin.ObjectID
import com.origin.TILE_SIZE
import com.origin.config.ServerConfig
import com.origin.model.action.Action
import com.origin.move.MoveController
import com.origin.move.MovingObject
import com.origin.net.ObjectAdd
import com.origin.net.ObjectMoved
import com.origin.net.ObjectStartMove
import com.origin.net.ObjectStopped
import com.origin.util.Vec2i

abstract class Human(id: ObjectID, pos: ObjectPosition) : MovingObject(id, pos) {

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
    protected val openedObjectsList by lazy { OpenedObjectsList(this) }

    /**
     * действие которое выполняет объект в данный момент
     */
    var action: Action? = null

    abstract val status: HumanStatus

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

    /**
     * последняя позиция в которой было обновление видимых объектов
     * нужно чтобы часто не обновлять список видимых (слишком накладно)
     */
    private var lastPosUpdateVisible: Vec2i? = null

    override suspend fun processMessage(msg: Any) {
        when (msg) {
            is BroadcastEvent.StartMove -> {
                if (this is Player && knownList.isKnownObject(msg.obj)) {
                    sendToSocket(ObjectStartMove(msg))
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
                        if (this is Player) sendToSocket(ObjectMoved(msg))
                    }
                } else {
                    // объект не знаем. но видим
                    if (isObjectVisibleForMe(msg.obj)) {
                        knownList.addKnownObject(msg.obj)
                        if (this is Player) sendToSocket(ObjectMoved(msg))
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
                        if (this is Player) sendToSocket(ObjectStopped(msg))
                    }
                } else {
                    // объект не знаем. но видим
                    if (isObjectVisibleForMe(msg.obj)) {
                        knownList.addKnownObject(msg.obj)
                        if (this is Player) sendToSocket(ObjectStopped(msg))
                    }
                }
            }

            is BroadcastEvent.Changed -> {
                if (knownList.isKnownObject(msg.obj)) {
                    if (this is Player) sendToSocket(ObjectAdd(msg.obj))
                }
            }

            is HumanMessage.StopAction -> stopAction()

            else -> super.processMessage(msg)
        }
    }

    override suspend fun afterSpawn() {
        super.afterSpawn()
        updateVisibleObjects(true)
        broadcastStatusUpdate()
    }

    override suspend fun onRemovedFromGrid() {
        knownList.clear()
        super.onRemovedFromGrid()
    }

    override suspend fun onGridObjectAdded(obj: GameObject) {
        if (isObjectVisibleForMe(obj)) {
            knownList.addKnownObject(obj)
        }
        super.onGridObjectAdded(obj)
    }

    override suspend fun onGridObjectRemoved(obj: GameObject) {
        logger.debug("human onObjectRemoved {}", obj)
        knownList.removeKnownObject(obj)
        super.onGridObjectRemoved(obj)
    }

    /**
     * Вижу ли я указанный объект
     */
    private fun isObjectVisibleForMe(obj: GameObject): Boolean {
        // себя всегда видим!
        return obj.id == this.id || pos.dist(obj.pos) < visibleDistance
    }

    suspend fun stopAction() {
        action?.stop()
        action = null
    }

    override suspend fun startMove(controller: MoveController) {
        openedObjectsList.closeAll()
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
        grid.sendAndWait(GridMessage.Activate(this))
    }

    override suspend fun onLeaveGrid(grid: Grid) {
        super.onLeaveGrid(grid)
        logger.warn("Deactivate ${grid.x} ${grid.y}")
        grid.sendAndWait(GridMessage.Deactivate(this))
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

    /**
     * отправить в окружающее пространство информацию о своем статусе, hp, stamina и тд
     */
    abstract fun broadcastStatusUpdate()
}