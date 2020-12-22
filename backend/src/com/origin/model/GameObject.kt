package com.origin.model

import com.origin.entity.EntityPosition

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
}