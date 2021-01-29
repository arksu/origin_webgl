package com.origin.utils

class Rect {
    var left: Int
        private set

    var top: Int
        private set

    var right: Int
        private set

    var bottom: Int
        private set

    constructor(left: Int, top: Int, right: Int, bottom: Int) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        normalize()
    }

    constructor(size: Int) {
        left = -size
        top = -size
        right = size
        bottom = size
    }

    private fun normalize() {
        if (right < left) {
            val t = left
            left = right
            right = t
        }
        if (bottom < top) {
            val t = top
            top = bottom
            bottom = t
        }
    }

    fun add(b: Rect) {
        left += b.left
        right += b.right
        top += b.top
        bottom += b.bottom
    }

    fun add(dist: Int) {
        left -= dist
        top -= dist
        right += dist
        bottom += dist
    }

    fun isPointInside(x: Int, y: Int): Boolean {
        return x >= left && x < right && y >= top && y < bottom
    }

    fun move(x: Int, y: Int): Rect {
        left += x
        right += x
        top += y
        bottom += y
        return this
    }

    fun clone(): Rect {
        return Rect(left, top, right, bottom)
    }

//    fun isIntersect(r2: Rect): Boolean {
//        return (left > r2.left && left <= r2.right || right > r2.left && right <= r2.right ||
//                r2.left > left && r2.left <= right || r2.right > left && r2.right <= right)
//                && (top > r2.top && top <= r2.bottom || bottom > r2.top && bottom <= r2.bottom ||
//                r2.top > top && r2.top <= bottom || r2.bottom > top && r2.bottom <= bottom)
//    }

    /**
     * пересекаются ли эти 2 прямоугольника
     * используется в детекте коллизий
     */
    fun isIntersect(r2: Rect): Boolean {
        return left < r2.right && right > r2.left && top < r2.bottom && bottom > r2.top
    }
}