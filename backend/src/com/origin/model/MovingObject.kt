package com.origin.model

import com.origin.entity.EntityPosition
import com.origin.entity.Grid
import com.origin.entity.GridMsg
import com.origin.net.logger
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.concurrent.ConcurrentLinkedQueue

sealed class MovingObjectMsg {
    class LoadGrids(val job: CompletableJob? = null);
    class UnloadGrids(val job: CompletableJob? = null);
}

/**
 * объект который может самостоятельно передвигаться
 */
@ObsoleteCoroutinesApi
open class MovingObject(pos: EntityPosition) : GameObject(pos) {


    /**
     * список гридов в которых находится объект. max 9 штук.
     */
    private val grids = ConcurrentLinkedQueue<Grid>()

    override suspend fun processMessages(msg: Any) {
        when (msg) {
            is MovingObjectMsg.LoadGrids -> {
                loadGrids()
                msg.job?.complete()
            }
            is MovingObjectMsg.UnloadGrids -> {
                uloadGrids()
                msg.job?.complete()
            }
            else -> super.processMessages(msg)
        }
    }

    /**
     * заполнить список гридов с которыми взаимодействует этот объект
     * вызываться может только если еще не был заполнен этот список
     * в случае телепорта объекта надо очистить этот список
     */
    private suspend fun loadGrids() {
        if (!grids.isEmpty()) {
            throw RuntimeException("activateGrids - grids is not empty")
        }

        // гриды рядом
        for (x in -1..1) for (y in -1..1) {
            val gx = pos.gridX + x
            val gy = pos.gridY + y
            if (grid.layer.validateCoord(gx, gy)) {
                val grid = World.instance.getGrid(pos.region, pos.level, gx, gy)
                grids.add(grid)

                if (this is Human) {
                    val h = this
                    logger.debug("GridMsg.Activate ${grid.x} ${grid.y}")
                    val job = Job()
                    grid.actor.send(GridMsg.Activate(h, job))
                    job.join()
                }
            }
        }
    }

    /**
     * выгрузить все гриды в которых находимся
     */
    private suspend fun uloadGrids() {
        if (grids.isEmpty()) {
            throw RuntimeException("uloadGrids - grids is empty")
        }

        if (this is Human) grids.forEach {
            val job = Job()
            it.actor.send(GridMsg.Deactivate(this, job))
            job.join()
        }
        grids.clear()
    }

    /**
     * изменился грид в котором находимся. надо отреагировать
     */
    fun gridChanged() {
    }

    fun onLeaveGrid() {

    }
}