package com.origin.model

import com.origin.entity.EntityPosition
import com.origin.entity.Grid

/**
 * базовый игровой объект в игровой механике
 * все игровые сущности наследуются от него
 */
open class GameObject(entityPosition: EntityPosition) {
    /**
     * координаты кэшируем в объекте (потом периодически обновляем в сущности)
     */
    val pos: Position = Position(entityPosition.x,
        entityPosition.y,
        entityPosition.level,
        entityPosition.region,
        entityPosition.heading,
        this)

    /**
     * текущий активный грид в котором находится объект
     */
    private val grid: Grid? get() = pos.grid

    /**
     * когда удален из грида
     */
    fun onRemove() {

    }
}