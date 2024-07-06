package com.origin.model

import com.origin.ObjectID
import com.origin.util.Vec2i
import java.util.*

abstract class MovingObject(id: ObjectID, pos: ObjectPosition) : GameObject(id, pos) {
    /**
     * список гридов в которых находится объект. max 9 штук.
     */
    protected val grids = LinkedList<Grid>()

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
            throw RuntimeException("activateGrids - grids is not empty")
        }
        val gr = grid ?: throw RuntimeException("not spawned to grid")
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
        val currentGrid = grid ?: throw IllegalStateException("grid is null")

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

    protected open suspend fun onEnterGrid(grid: Grid) {
    }

    protected open suspend fun onLeaveGrid(grid: Grid) {
    }
}