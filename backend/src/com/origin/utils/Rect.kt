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

    fun move(b: Rect) {
        left += b.left
        right += b.right
        top += b.top
        bottom += b.bottom
    }

    /**
     * добавить (увеличить в размере)
     */
    fun move(dist: Int) {
        left -= dist
        top -= dist
        right += dist
        bottom += dist
    }

    /**
     * добавить (переместить) по координатам
     */
    fun move(p: Vec2i): Rect {
        left += p.x
        right += p.x
        top += p.y
        bottom += p.y
        return this
    }

    fun move(x: Int, y: Int): Rect {
        left += x
        right += x
        top += y
        bottom += y
        return this
    }

    /**
     * расширить (меньше нуля расширяют влево, вверх. больше нуля вправо, вниз)
     */
    fun extend(x: Int, y: Int): Rect {
        if (x < 0) left += x else right += x
        if (y < 0) top += y else bottom += y
        return this
    }

    fun extendSize(w: Int, h: Int): Rect {
        left -= w
        right += w
        top -= h
        bottom += h
        return this
    }

    /**
     * минимальное расстояние между двумя прямоугольниками
     * если прямоугольник перескается по какой то из осей - вернет -1
     */
    fun min(r: Rect): Pair<Int, Int> {
        val dx =
            if (left < r.right && right > r.left) -1 else Math.min(Math.abs(left - r.right), Math.abs(right - r.left))
        val dy =
            if (top < r.bottom && bottom > r.top) -1 else Math.min(Math.abs(top - r.bottom), Math.abs(bottom - r.top))

        return Pair(dx, dy)
    }

    fun isPointInside(x: Int, y: Int): Boolean {
        return x >= left && x < right && y >= top && y < bottom
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

    override fun toString(): String {
        return "($left $top $right $bottom)"
    }
}