package com.origin.model.move

import com.origin.collision.CollisionResult
import com.origin.model.GameObject
import com.origin.model.Grid
import com.origin.model.GridMsg
import com.origin.model.World
import com.origin.utils.GRID_FULL_SIZE
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * позиция объекта в игровом мире
 */
@ObsoleteCoroutinesApi
class Position(
    var x: Int,
    var y: Int,
    var level: Int,
    var region: Int,
    var heading: Int,
    val parent: GameObject,
) {

    /**
     * грид в котором находится объект
     * либо null если еще не привязан к гриду (не заспавнен)
     */
    lateinit var grid: Grid
        private set

    /**
     * координаты грида
     */
    val gridX = x / GRID_FULL_SIZE
    val gridY = y / GRID_FULL_SIZE

    /**
     * заспавнить объект в мир
     */
    suspend fun spawn(): Boolean {
        if (::grid.isInitialized) {
            throw RuntimeException("pos.grid is already set, on spawn")
        }
        // берем грид и спавнимся через него
        val g = World.getGrid(this)

        val resp = CompletableDeferred<CollisionResult>()
        g.send(GridMsg.Spawn(parent, resp))
        val result = resp.await()

        // если успешно добавились в грид - запомним его у себя
        return if (result.result == CollisionResult.CollisionType.COLLISION_NONE) {
            grid = g
            true
        } else {
            false
        }
    }

    /**
     * получить дистанцию между позициями двух объектов
     * @param other позиция другого объекта
     * @return дистанция в единицах координат
     */
    fun getDistance(other: Position): Int {
        return if (level != other.level) {
            Int.MAX_VALUE
        } else {
            sqrt((other.x - x).toDouble().pow(2) + (other.y - y).toDouble().pow(2)).roundToInt()
        }
    }

    fun setXY(x: Double, y: Double) {
        setXY(x.roundToInt(), y.roundToInt())
    }

    private fun setXY(x: Int, y: Int) {
        this.x = x
        this.y = y
        updateGrid()
    }

    private fun updateGrid() {

    }

    fun clone(): Position {
        return Position(x, y, level, region, heading, parent)
    }
}