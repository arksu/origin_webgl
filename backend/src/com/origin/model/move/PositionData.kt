package com.origin.model.move

import com.origin.model.Grid
import com.origin.utils.GRID_FULL_SIZE
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
data class PositionData(
    val x: Int,
    val y: Int,
    var level: Int,
    var region: Int,
    var heading: Short
) {
    /**
     * координаты грида
     */
    val gridX get() = x / GRID_FULL_SIZE
    val gridY get() = y / GRID_FULL_SIZE

    constructor(x: Int, y: Int, p: Position) : this(x, y, p.level, p.region, p.heading)

    constructor(x: Int, y: Int, g: Grid) : this(x, y, g.level, g.region, 0)
}