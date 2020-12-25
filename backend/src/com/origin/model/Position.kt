package com.origin.model

import com.origin.entity.Grid
import com.origin.utils.GRID_FULL_SIZE

/**
 * позиция объекта в игровом мире
 */
class Position(
    var x: Int,
    var y: Int,
    var level: Int,
    var region: Int,
    var heading: Int,
    val parent: GameObject,
) {

    /**
     * грид в котором находится объект
     * либо null если еще не привязан к гриду (не заспавнен)
     */
    var grid: Grid? = null

    /**
     * грид
     */
    val gridX = x / GRID_FULL_SIZE
    val gridY = y / GRID_FULL_SIZE

    /**
     * заспавнить объект в мир
     */
    fun spawn(): Boolean {
        // берем грид и спавнимся через него
        return World.instance.getGrid(this).spawn(parent)
    }
}