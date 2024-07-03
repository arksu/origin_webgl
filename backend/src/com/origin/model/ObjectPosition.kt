package com.origin.model

import com.origin.GRID_FULL_SIZE
import com.origin.util.Vec2i
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ObjectPosition(
    initX: Int,
    initY: Int,
    var level: Int,
    var region: Int,
    var heading: Byte,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ObjectPosition::class.java)
    }

    val point = Vec2i(initX, initY)

    val x get() = point.x
    val y get() = point.y

    /**
     * координаты грида
     */
    val gridX get() = point.x / GRID_FULL_SIZE
    val gridY get() = point.y / GRID_FULL_SIZE

    /**
     * грид в котором находится объект
     * либо null если еще не привязан к гриду (не заспавнен)
     * по этому полю детектим заспавнен ли объект в мир
     */
    var grid: Grid? = null
        private set

    fun setGrid(grid: Grid) {
        this.grid = grid
    }
}