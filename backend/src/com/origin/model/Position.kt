package com.origin.model

import com.origin.utils.GRID_FULL_SIZE

/**
 * позиция объекта в игровом мире
 */
class Position(var x: Int, var y: Int, var level: Int, var region: Int, var heading: Int, val parent: GameObject) {
    val gridX = x / GRID_FULL_SIZE
    val gridY = y / GRID_FULL_SIZE
}