package com.origin.model

import com.origin.GRID_FULL_SIZE
import com.origin.util.Vec2i
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ObjectPosition {
    var level: Int
    var region: Int
    var heading: Byte

    constructor(initX: Int, initY: Int, level: Int, region: Int, heading: Byte) {
        this.level = level
        this.region = region
        this.heading = heading
        this.point = Vec2i(initX, initY)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ObjectPosition::class.java)
    }

    constructor(x: Int, y: Int, pos: ObjectPosition) {
        point = Vec2i(x, y)
        this.region = pos.region
        this.heading = pos.heading
        this.level = pos.level
    }

    val point: Vec2i

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

    override fun toString(): String {
        return "{pos $x, $y level=$level region=$region, heading=$heading}"
    }
}