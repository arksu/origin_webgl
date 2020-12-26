package com.origin.model

import com.origin.entity.Grid
import com.origin.entity.GridMsg
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
    var grid: Grid? = null
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
        // берем грид и спавнимся через него
        val g = World.instance.getGrid(this)

        val deferred = CompletableDeferred<CollisionResult>()
        g.actor.send(GridMsg.Spawn(parent, deferred))
        val result = deferred.await()

        // если успешно добавились в грид - запомним его у себя
        return if (result.result == CollisionResult.CollisionType.COLLISION_NONE) {
            grid = g
            true
        } else {
            false
        }
    }
}