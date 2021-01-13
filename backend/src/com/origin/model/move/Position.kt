package com.origin.model.move

import com.origin.collision.CollisionResult
import com.origin.model.GameObject
import com.origin.model.Grid
import com.origin.model.GridMsg
import com.origin.model.World
import com.origin.utils.GRID_FULL_SIZE
import com.origin.utils.Vec2i
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

/**
 * позиция объекта в игровом мире
 */
@ObsoleteCoroutinesApi
class Position(
    initx: Int,
    inity: Int,
    var level: Int,
    var region: Int,
    var heading: Int,
    val parent: GameObject,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Position::class.java)
    }

    val point = Vec2i(initx, inity)

    val x get() = point.x
    val y get() = point.y

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

    fun dist(other: Position): Int {
        return point.dist(other.point);
    }

    fun setXY(x: Double, y: Double) {
        setXY(x.roundToInt(), y.roundToInt())
    }

    fun setXY(x: Int, y: Int) {
        logger.debug("setXY")
        this.point.x = x
        this.point.y = y
        updateGrid()
    }

    private fun updateGrid() {
        // TODO updateGrid
    }

    fun clone(): Position {
        return Position(x, y, level, region, heading, parent)
    }
}