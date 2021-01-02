package com.origin.model

import com.origin.entity.EntityPosition
import com.origin.model.GridMsg.Activate
import com.origin.model.GridMsg.Deactivate
import com.origin.utils.ObjectID
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

sealed class MovingObjectMsg {
    class LoadGrids(job: CompletableJob) : MessageWithJob(job)
    class UnloadGrids(job: CompletableJob? = null) : MessageWithJob(job)
}

/**
 * объект который может самостоятельно передвигаться
 */
@ObsoleteCoroutinesApi
open class MovingObject(id: ObjectID, pos: EntityPosition) : GameObject(id, pos) {
    /**
     * список гридов в которых находится объект. max 9 штук.
     */
    private val grids = LinkedList<Grid>()

    override suspend fun processMessages(msg: Any) {
        logger.warn("MovingObject processMessages ${msg.javaClass.simpleName}")

        when (msg) {
            is MovingObjectMsg.LoadGrids -> {
                loadGrids()
                msg.job?.complete()
            }
            is MovingObjectMsg.UnloadGrids -> {
                unloadGrids()
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
        // грузить гриды можем только если ничего еще не было загружено
        if (!grids.isEmpty()) {
            throw RuntimeException("activateGrids - grids is not empty")
        }

        // гриды рядом
        for (x in -1..1) for (y in -1..1) {
            val gx = pos.gridX + x
            val gy = pos.gridY + y
            if (grid.layer.validateCoord(gx, gy)) {
                val grid = World.getGrid(pos.region, pos.level, gx, gy)
                grids.add(grid)

                if (this is Human) {
                    val h = this
                    logger.debug("GridMsg.Activate ${grid.x} ${grid.y}")
                    grid.sendJob(Activate(h, Job())).join()
                }
            }
        }
    }

    /**
     * выгрузить все гриды в которых находимся
     */
    private suspend fun unloadGrids() {
        if (grids.isEmpty()) {
            throw RuntimeException("uloadGrids - grids is empty")
        }

        if (this is Human) grids.forEach {
            grid.sendJob(Deactivate(this, Job())).join()
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