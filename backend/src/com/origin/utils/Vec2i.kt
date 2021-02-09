package com.origin.utils

import kotlin.math.atan
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * vector of 2 int
 */
data class Vec2i(
    var x: Int,
    var y: Int,
) {

    fun add(ax: Int, ay: Int): Vec2i {
        return Vec2i(x + ax, y + ay)
    }

    fun add(p: Vec2i): Vec2i {
        return add(p.x, p.y)
    }

    fun sub(ax: Int, ay: Int): Vec2i {
        return Vec2i(x - ax, y - ay)
    }

    fun sub(p: Vec2i): Vec2i {
        return sub(p.x, p.y)
    }

    fun mul(v: Int): Vec2i {
        return Vec2i(x * v, y * v)
    }

    fun mul(ax: Int, ay: Int): Vec2i {
        return Vec2i(x * ax, y * ay)
    }

    fun div(v: Int): Vec2i {
        return Vec2i(x / v, y / v)
    }

    fun mod(v: Int): Vec2i {
        return Vec2i(x % v, y % v)
    }


    /**
     * получить направление на указанную точку
     * угол направления в градусах
     */
    fun dir(p: Vec2i): Int {
        val vector = p.sub(this)
        if (vector.x == 0 && vector.y == 0) return 0
        var a = atan(vector.x.toDouble() / vector.y.toDouble())
        if (a < 0) {
            a += Math.PI + Math.PI
        }
        return Math.toDegrees(a).roundToInt()
    }

    /**
     * длина вектора
     */
    fun len(): Double {
        return sqrt((x * x + y * y).toDouble())
    }

    /**
     * дистанция между этой точкой и другой
     */
    fun dist(p: Vec2i): Int {
        val dx = p.x - x
        val dy = p.y - y
        return sqrt((dx * dx + dy * dy).toDouble()).roundToInt()
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}