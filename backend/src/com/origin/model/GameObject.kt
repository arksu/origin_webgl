package com.origin.model

/**
 * базовый игровой объект в игровой механике
 * все игровые сущности наследуются от него
 */
abstract class GameObject {
    /**
     * работа с координатами
     */
    abstract var x: Int
    abstract var y: Int
    abstract var level: Int
    abstract var region: Int
}