package com.origin.move

import com.origin.ObjectID
import com.origin.TimeController
import com.origin.model.*
import com.origin.util.Vec2i
import java.util.*

abstract class MovingObject(id: ObjectID, pos: ObjectPosition) : GameObject(id, pos) {
    /**
     * список гридов в которых находится объект. max 9 штук.
     */
    protected val grids = LinkedList<Grid>()

    /**
     * контроллер который управляет передвижением объекта
     */
    private var moveController: MoveController? = null


    override suspend fun processMessage(msg: Any) {
        when (msg) {
            is MovingObjectMessage.UpdateMove -> onUpdateMove()
            else -> super.processMessage(msg)
        }
    }

    /**
     * начать движение объекта
     */
    open suspend fun startMove(controller: MoveController) {
        logger.debug("startMove")
        val old = moveController
        if (old != null) {
            old.updateAndResult()
            old.stop()
        }
        if (controller.canStartMoving()) {
            moveController = controller
            controller.start()
        } else {
            logger.debug("can't start move {}", this)
        }
    }

    open suspend fun stopMove() {
        logger.warn("stopMove")
        moveController?.stop()
        storePositionInDb()
        moveController = null

        getGridSafety().broadcast(BroadcastEvent.Stopped(this))
    }

    /**
     * обработка движения от TimeController
     */
    private suspend fun onUpdateMove() {
        if (moveController != null) logger.debug("updateMove")
        val result = moveController?.updateAndResult()
        // если контроллера нет. либо он завершил работу
        if (result == null || result == true) {
            TimeController.deleteMovingObject(this)
            moveController = null
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
     * удаление объекта
     */
    override suspend fun remove() {
        // deactivate and unload grids
        unloadGrids()

        // TODO
//        moveController?.stop()
        super.remove()
    }

    /**
     * заполнить список гридов с которыми взаимодействует этот объект
     * вызываться может только если еще не был заполнен этот список
     * в случае телепорта объекта надо очистить этот список
     */
    protected open suspend fun loadGrids() {
        // грузить гриды можем только если ничего еще не было загружено
        if (!grids.isEmpty()) {
            throw RuntimeException("loadGrids - grids is not empty")
        }
        val gr = getGridSafety()
        // гриды рядом
        for (x in -1..1) for (y in -1..1) {
            val gx = pos.gridX + x
            val gy = pos.gridY + y
            if (gr.layer.validateCoord(gx, gy)) {
                val g = World.getGrid(pos.region, pos.level, gx, gy)
                grids.add(g)
                // TODO выполнять onEnterGrid параллельно в корутинах
                onEnterGrid(g)
            }
        }
    }

    /**
     * выгрузить все гриды в которых находимся
     */
    private suspend fun unloadGrids() {
        if (this is Human) grids.forEach {
            onLeaveGrid(it)
        }
        grids.clear()
    }

    /**
     * изменился грид в котором находимся. надо отреагировать
     */
    open suspend fun onGridChanged() {
        val currentGrid = getGridSafety()

        // новый список гридов в которых находимся (координаты)
        val newList = ArrayList<Vec2i>(5)
        // идем вокруг нового грида
        for (x in -1..1) for (y in -1..1) {
            val gx = currentGrid.x + x
            val gy = currentGrid.y + y

            // если координаты не валидные - продолжаем дальше
            if (!currentGrid.layer.validateCoord(gx, gy)) continue

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
            val toRemove = LinkedHashSet<Grid>(5)
            for (g in grids) {
                if (!newList.contains(g.pos)) {
                    onLeaveGrid(g)
                    toRemove.add(g)
                }
            }
            grids.removeAll(toRemove)
        }
    }

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
            MoveMode.CRAWL -> 18.0 // 1.5 tiles per sec
            MoveMode.WALK -> 36.0 // 3.0 tiles per sec
            MoveMode.RUN -> 54.0 // 4.5 tiles per sec
            MoveMode.SPRINT -> 72.0 // 6.0 tiles per sec
        }
        // по воде движемся в 2 раза медленее
        return if (getMovementType() == MoveType.SWIMMING) s / 2 else s
    }

    /**
     * сохранить позицию объекта в базу (вызывается периодически в движении)
     */
    abstract fun storePositionInDb()

    protected open suspend fun onEnterGrid(grid: Grid) {
    }

    protected open suspend fun onLeaveGrid(grid: Grid) {
    }
}