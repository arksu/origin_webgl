package com.origin.model

import com.origin.TimeController
import com.origin.model.move.MoveController
import com.origin.model.move.MoveMode
import com.origin.model.move.MoveType
import com.origin.utils.ObjectID
import com.origin.utils.Vec2i
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*
import kotlin.collections.ArrayList

sealed class MovingObjectMsg {
    class UpdateMove
}

/**
 * объект который может самостоятельно передвигаться
 */
@ObsoleteCoroutinesApi
abstract class MovingObject(id: ObjectID, x: Int, y: Int, level: Int, region: Int, heading: Short) :
    GameObject(id, x, y, level, region, heading) {

    /**
     * список гридов в которых находится объект. max 9 штук.
     */
    protected val grids = LinkedList<Grid>()

    /**
     * контроллер который управляет передвижением объекта
     */
    private var moveController: MoveController? = null

    /**
     * объект с которым "столкнулись" (прилинковались), может быть виртуальный или реальный
     * если реальный, то при удалении его из known списка должны занулить и здесь.
     * то есть это реальный объект с которым мы взаимодействуем
     */
    private var linkedObject: GameObject? = null

    override suspend fun processMessage(msg: Any) {
//        logger.debug("MovingObject processMessage ${msg.javaClass.simpleName}")

        when (msg) {
            is MovingObjectMsg.UpdateMove -> updateMove()
            else -> super.processMessage(msg)
        }
    }

    /**
     * после спавна сразу загружаем список гридов вокруг
     */
    override suspend fun afterSpawn() {
        super.afterSpawn()
        loadGrids()
    }

    /**
     * заполнить список гридов с которыми взаимодействует этот объект
     * вызываться может только если еще не был заполнен этот список
     * в случае телепорта объекта надо очистить этот список
     */
    protected open suspend fun loadGrids() {
        // грузить гриды можем только если ничего еще не было загружено
        if (!grids.isEmpty()) {
            throw RuntimeException("activateGrids - grids is not empty")
        }

        // гриды рядом
        for (x in -1..1) for (y in -1..1) {
            val gx = pos.gridX + x
            val gy = pos.gridY + y
            if (grid.layer.validateCoord(gx, gy)) {
                val g = World.getGrid(pos.region, pos.level, gx, gy)
                grids.add(g)
                onEnterGrid(g)
            }
        }
    }

    /**
     * выгрузить все гриды в которых находимся
     */
    private suspend fun unloadGrids() {
//        if (grids.isEmpty()) {
//            throw RuntimeException("unloadGrids - grids is empty")
//        }

        if (this is Human) grids.forEach {
            onLeaveGrid(it)
        }
        grids.clear()
    }

    /**
     * начать движение объекта
     */
    open suspend fun startMove(controller: MoveController) {
        val old = moveController
        if (old != null) {
            old.updateAndResult()
            old.stop()
        }
        if (controller.canStartMoving()) {
            moveController = controller
            controller.start()
        } else {
            logger.debug("cant start move $this")
        }
    }

    open suspend fun stopMove() {
        logger.warn("stopMove")
        moveController?.stop()
        storePositionInDb()
        moveController = null

        grid.broadcast(BroadcastEvent.Stopped(this))
    }

    /**
     * обработка движения от TimeController
     */
    private suspend fun updateMove() {
        val result = moveController?.updateAndResult()
        // если контроллера нет. либо он завершил работу
        if (result == null || result) {
            TimeController.deleteMovingObject(this)
        }
    }

    /**
     * удаление объекта
     */
    override suspend fun remove() {
        // deactivate and unload grids
        unloadGrids()

        moveController?.stop()
        super.remove()
    }

    /**
     * сохранить позицию объекта в базу (вызывается периодически в движении)
     */
    abstract fun storePositionInDb()

    /**
     * текущий режим перемещения объекта
     */
    protected open fun getMovementMode(): MoveMode {
        return MoveMode.WALK
    }

    /**
     * текущий тип движения (идем, плывем и тд)
     */
    fun getMovementType(): MoveType {
        // TODO проверить тайл подо мной, если это вода то SWIM иначе WALK
        return MoveType.WALK
    }

    /**
     * текущая скорость передвижения (используется при вычислении перемещения за единицу времени)
     * тут надо учитывать статы и текущий режим перемещения
     * сколько игровых координат проходим за 1 реальную секунду
     */
    fun getMovementSpeed(): Double {
        // TODO : смотреть тайл, если мощеный камень - увеличиваем скорость
        val s = when (getMovementMode()) {
            MoveMode.STEAL -> 25.0
            MoveMode.WALK -> 62.0
            MoveMode.RUN -> 120.0
        }
        // по воде движемся в 2 раза медленее
        return if (getMovementType() == MoveType.SWIMMING) s / 2 else s
    }

    /**
     * изменился грид в котором находимся. надо отреагировать
     */
    open suspend fun onGridChanged() {
        // новый список гридов в которых находимся (координаты)
        val newList = ArrayList<Vec2i>(5)
        // идем вокруг нового грида
        for (x in -1..1) for (y in -1..1) {
            val gx = grid.x + x
            val gy = grid.y + y

            // если координаты не валидные - продолжаем дальше
            if (!grid.layer.validateCoord(gx, gy)) continue

            // добавим координаты в список новых гридов
            newList.add(Vec2i(gx, gy))

            // ищем среди текущих гридов
            var found = false
            for (g in grids) {
                if (g.x == gx && g.y == gy) {
                    found = true
                    break
                }
            }
            // если грида с такими координатами еще не было
            if (!found) {
                // получим его из мира
                val grid = World.getGrid(pos.region, pos.level, gx, gy)
                // и добавим в список
                grids.add(grid)
                onEnterGrid(grid)
            }
        }
        if (newList.isNotEmpty()) {
            val toRemove = ArrayList<Grid>(5)
            for (g in grids) {
                if (!newList.contains(g.pos)) {
                    onLeaveGrid(g)
                    toRemove.add(g)
                }
            }
            grids.removeAll(toRemove)
        }
    }

    protected open suspend fun onEnterGrid(grid: Grid) {
    }

    protected open suspend fun onLeaveGrid(grid: Grid) {
    }

    open fun clearLinkedObject() {
        linkedObject = null
    }
}