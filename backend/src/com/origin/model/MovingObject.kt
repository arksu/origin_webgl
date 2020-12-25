package com.origin.model

import com.origin.entity.EntityPosition
import com.origin.entity.Grid
import com.origin.net.model.GameResponse
import com.origin.net.model.MapGridData
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.withLock

/**
 * объект который может самостоятельно передвигаться
 */
open class MovingObject(pos: EntityPosition) : GameObject(pos) {

    /**
     * список гридов в которых находится объект. max 9 штук.
     */
    protected val grids = ConcurrentLinkedQueue<Grid>()

    /**
     * заполнить список гридов с которыми взаимодействует этот объект
     * вызываться может только если еще не был заполнен этот список
     * в случае телепорта объекта надо очистить этот список
     */
    suspend fun loadGrids() {
        lock.withLock {
            if (!grids.isEmpty()) {
                throw RuntimeException("activateGrids - grids is not empty")
            }

            // гриды рядом
            for (x in -1..1) for (y in -1..1) {
                val gx = pos.gridX + x
                val gy = pos.gridY + y
                if (grid!!.layer.validateCoord(gx, gy)) {
                    val grid = World.instance.getGrid(pos.region, pos.level, gx, gy)
                    grids.add(grid)

                    if (this is Human) {
                        grid.lock.withLock {
                            grid.activate(this)
                        }
                    }
                }
            }
        }
        if (this is Player) grids.forEach {
            this.session.send(GameResponse("map", MapGridData(it)))
        }
    }

    /**
     * выгрузить все гриды в которых находимся
     */
    fun uloadGrids() {
        lock.withLock {
            if (grids.isEmpty()) {
                throw RuntimeException("uloadGrids - grids is empty")
            }
            if (this is Human) grids.forEach {
                it.lock.withLock {
                    it.deactivate(this)
                }
            }
            grids.clear()
        }
    }

    /**
     * изменился грид в котором находимся. надо отреагировать
     */
    fun gridChanged() {
    }

    fun onLeaveGrid() {

    }
}