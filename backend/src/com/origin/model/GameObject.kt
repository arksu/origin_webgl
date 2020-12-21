package com.origin.model

/**
 * базовый игровой объект в игровой механике
 * все игровые сущности наследуются от него
 */
open class GameObject(x: Int, y: Int, level: Int, region: Int, heading: Int) {
    /**
     * координаты кэшируем в объекте (потом периодически обновляем в сущности)
     */
    val coord: Coord = Coord(x, y, level, region, heading, this)

    val x get() = coord.x
    val y get() = coord.y
    val level get() = coord.level
    val region get() = coord.region
}