package com.origin.model

import com.origin.entity.EntityPosition
import com.origin.entity.Grid
import java.util.concurrent.locks.ReentrantLock

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
     * блокировка для операций с объектом
     */
    val lock = ReentrantLock()

    /**
     * текущий активный грид в котором находится объект
     */
    protected val grid: Grid? get() = pos.grid

    /**
     * когда этот объект удален из грида
     */
    fun onRemove() {
        // TODO known list
    }
}