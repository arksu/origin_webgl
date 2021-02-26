package com.origin.model.move

import com.origin.collision.CollisionResult
import com.origin.model.*
import com.origin.utils.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * позиция объекта в игровом мире
 */
@ObsoleteCoroutinesApi
class Position(
    initX: Int,
    initY: Int,
    var level: Int,
    var region: Int,
    var heading: Short,
    private val parent: GameObject,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Position::class.java)
    }

    constructor(ix: Int, iy: Int, pos: Position) : this(ix, iy, pos.level, pos.region, pos.heading, pos.parent)

    val point = Vec2i(initX, initY)

    val x get() = point.x
    val y get() = point.y

    /**
     * грид в котором находится объект
     * либо null если еще не привязан к гриду (не заспавнен)
     * по этому полю детектим заспавнен ли объект в мир
     */
    lateinit var grid: Grid
        private set

    /**
     * координаты грида
     */
    val gridX get() = point.x / GRID_FULL_SIZE
    val gridY get() = point.y / GRID_FULL_SIZE

    /**
     * индекс тайла грида в котором находятся данные координаты
     */
    val tileIndex: Int
        get() {
            val p = point.mod(GRID_FULL_SIZE).div(TILE_SIZE)
            return p.x + p.y * GRID_SIZE
        }

    /**
     * заспавнить объект в мир
     */
    suspend fun spawn(): Boolean {
        if (isSpawned()) {
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
     * заспавниться рядом.
     * если удалось - координаты изменятся
     * если не удалось координаты останутся оригинальные.
     */
    suspend fun spawnNear(): Boolean {
        val origX = point.x
        val origY = point.y

        val len = 2 * TILE_SIZE
        var success = false
        for (t in 0 until 10) {
            var dx = Rnd.next(len * 2) - len
            var dy = Rnd.next(len * 2) - len

            if (dx < 0 && dx > -TILE_SIZE) dx -= TILE_SIZE
            if (dx > 0 && dx < TILE_SIZE) dx += TILE_SIZE
            if (dy < 0 && dy > -TILE_SIZE) dy -= TILE_SIZE
            if (dy > 0 && dy < TILE_SIZE) dy += TILE_SIZE

            point.x += dx
            point.y += dy
            if (spawn()) {
                success = true
                break
            }
        }

        if (!success) {
            point.x = origX
            point.y = origY
        }
        return success
    }

    fun setGrid(grid: Grid) {
        this.grid = grid
    }

    fun initGrid() {
        this.grid = World.getGrid(this)
    }

    fun dist(other: Position): Double = point.dist(other.point)

    fun dist(px: Int, py: Int): Double = point.dist(px, py)

    /**
     * установка новых координат
     */
    suspend fun setXY(x: Int, y: Int) {
        logger.debug("setXY $x $y")

        // поставим новые координаты
        this.point.x = x
        this.point.y = y

        if (isSpawned()) {
            // запомним координаты старого грида
            val oldGx = gridX
            val oldGy = gridY

            // если координаты грида изменились
            if (oldGx != gridX || oldGy != gridY) {
                val old = grid
                // получим новый грид из мира
                grid = World.getGrid(this)
                if (parent is MovingObject) {
                    // уведомим объект о смене грида
                    parent.onGridChanged()
                }
                old.objects.remove(parent)
                grid.objects.add(parent)
            }
        }
    }

    /**
     * заспавнен ли объект в мир?
     */
    private fun isSpawned(): Boolean {
        return ::grid.isInitialized
    }

    override fun toString(): String {
        return "{pos $level $x $y ${this.hashCode()} $parent $point ${point.x} ${point.y} }"
    }
}