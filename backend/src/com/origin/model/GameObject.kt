package com.origin.model

/**
 * базовый игровой объект в игровой механике
 * все игровые сущности наследуются от него
 */
abstract class GameObject {
    /**
     * работа с координатами
     */
    abstract val x: Int
    abstract val y: Int
    abstract val level: Int
    abstract val instanceId: Int
}