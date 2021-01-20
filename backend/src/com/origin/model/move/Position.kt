package com.origin.model.move

import com.origin.collision.CollisionResult
import com.origin.model.*
import com.origin.utils.GRID_FULL_SIZE
import com.origin.utils.Vec2i
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * позиция объекта в игровом мире
 */
@ObsoleteCoroutinesApi
class Position(
    initx: Int,
    inity: Int,
    var level: Int,
    var region: Int,
    var heading: Short,
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
    val gridX get() = point.x / GRID_FULL_SIZE
    val gridY get() = point.y / GRID_FULL_SIZE

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

    /**
     * установка новых координат
     */
    suspend fun setXY(x: Int, y: Int) {
        logger.debug("setXY")

        // запомним координаты старого грида
        val oldgx = gridX
        val oldgy = gridY

        // поставим новые координаты
        this.point.x = x
        this.point.y = y

        // если координаты грида изменились
        if (oldgx != gridX || oldgy != gridY) {
            // получим новый грид из мира
            grid = World.getGrid(this)
            if (parent is MovingObject) {
                // уведомим объект о смене грида
                parent.onGridChanged()
            }
        }
    }

    override fun toString(): String {
        return "{pos $level $x $y ${this.hashCode()} $parent $point ${point.x} ${point.y} }"
    }
}