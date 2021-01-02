package com.origin.model

import com.origin.collision.CollisionResult
import com.origin.utils.GRID_FULL_SIZE
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi

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
}