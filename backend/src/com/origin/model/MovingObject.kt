package com.origin.model

import com.origin.entity.EntityPosition
import com.origin.entity.Grid

/**
 * объект который может самостоятельно передвигаться
 */
open class MovingObject(pos: EntityPosition) : GameObject(pos) {

    /**
     * список гридов в которых находится объект. max 9 штук.
     */
    protected val grids = ArrayList<Grid>(9)

    open fun activateGrids() {
        if (!grids.isEmpty()) {
            throw RuntimeException("activateGrids - grids is not empty")
        }

        for (x in -1..1) for (y in -1..1) {
            val gx = pos.gridX + x
            val gy = pos.gridY + y
            if (grid!!.layer.validateCoord(gx, gy)) {
                val grid = World.instance.getGrid(pos.region, pos.level, gx, gy)
                grids.add(grid)
            }
        }
    }

    fun onLeaveGrid() {

    }
}