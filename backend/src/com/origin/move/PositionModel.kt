package com.origin.move

import com.origin.GRID_FULL_SIZE
import com.origin.model.Grid
import com.origin.model.ObjectPosition

data class PositionModel(
    val x: Int,
    val y: Int,
    var level: Int,
    var region: Int,
    var heading: Byte
) {
    /**
     * координаты грида
     */
    val gridX get() = x / GRID_FULL_SIZE
    val gridY get() = y / GRID_FULL_SIZE

    constructor(x: Int, y: Int, p: ObjectPosition) : this(x, y, p.level, p.region, p.heading)

    constructor(x: Int, y: Int, g: Grid) : this(x, y, g.level, g.region, 0)
}
