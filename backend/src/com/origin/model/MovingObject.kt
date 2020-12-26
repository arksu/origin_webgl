package com.origin.model

import com.origin.entity.EntityPosition
import com.origin.entity.Grid
import com.origin.entity.GridMsg
import com.origin.net.logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.withLock

/**
 * объект который может самостоятельно передвигаться
 */
@ObsoleteCoroutinesApi
open class MovingObject(pos: EntityPosition) : GameObject(pos) {

    /**
     * список гридов в которых находится объект. max 9 штук.
     */
    protected val grids = ConcurrentLinkedQueue<Grid>()

    /**
     * заполнить список гридов с которыми взаимодействует этот объект
     * вызываться может только если еще не был заполнен этот список
     * в случае телепорта объекта надо очистить этот список
     */
    suspend fun loadGrids() {
        if (!grids.isEmpty()) {
            throw RuntimeException("activateGrids - grids is not empty")
        }

        // гриды рядом
        for (x in -1..1) for (y in -1..1) {
            val gx = pos.gridX + x
            val gy = pos.gridY + y
            if (grid!!.layer.validateCoord(gx, gy)) {
                val grid = World.instance.getGrid(pos.region, pos.level, gx, gy)
                grids.add(grid)

                if (this is Human) {
                    val h = this
                    logger.debug("GridMsg.Activate ${grid.x} ${grid.y}")
                    val defered = CompletableDeferred<Boolean>()
                    grid.actor.send(GridMsg.Activate(h, defered))
                    defered.await()
                }
            }
        }
    }

    /**
     * выгрузить все гриды в которых находимся
     */
    fun uloadGrids() {
        lock.withLock {
            if (grids.isEmpty()) {
                throw RuntimeException("uloadGrids - grids is empty")
            }
            if (this is Human) grids.forEach {
                it.lock.withLock {
                    it.deactivate(this)
                }
            }
            grids.clear()
        }
    }

    /**
     * изменился грид в котором находимся. надо отреагировать
     */
    fun gridChanged() {
    }

    fun onLeaveGrid() {

    }
}