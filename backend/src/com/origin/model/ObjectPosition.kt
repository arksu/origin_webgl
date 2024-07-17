package com.origin.model

import com.origin.GRID_FULL_SIZE
import com.origin.util.Vec2i
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ObjectPosition(
    initX: Int,
    initY: Int,
    var level: Int,
    var region: Int,
    var heading: Byte,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ObjectPosition::class.java)
    }

    val point = Vec2i(initX, initY)

    val x get() = point.x
    val y get() = point.y

    /**
     * координаты грида
     */
    val gridX get() = point.x / GRID_FULL_SIZE
    val gridY get() = point.y / GRID_FULL_SIZE

    fun dist(other: ObjectPosition): Double = point.dist(other.point)

    fun dist(px: Int, py: Int): Double = point.dist(px, py)

    fun clone(): ObjectPosition {
        return ObjectPosition(x, y, level, region, heading)
    }

    fun add(x: Int, y: Int) {
        point.x += x
        point.y += y
    }

    override fun toString(): String {
        return "{pos level=$level $x, $y ${this.hashCode()}}"
    }
}