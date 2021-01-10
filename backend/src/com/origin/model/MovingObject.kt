package com.origin.model

import com.origin.TimeController
import com.origin.model.GridMsg.Activate
import com.origin.model.GridMsg.Deactivate
import com.origin.model.move.MoveController
import com.origin.model.move.MoveMode
import com.origin.model.move.MoveType
import com.origin.utils.ObjectID
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

sealed class MovingObjectMsg {
    class UpdateMove
    class LoadGrids(job: CompletableJob) : MessageWithJob(job)
    class UnloadGrids(job: CompletableJob? = null) : MessageWithJob(job)
}

/**
 * объект который может самостоятельно передвигаться
 */
@ObsoleteCoroutinesApi
abstract class MovingObject(id: ObjectID, x: Int, y: Int, level: Int, region: Int, heading: Int) :
    GameObject(id, x, y, level, region, heading) {
    /**
     * список гридов в которых находится объект. max 9 штук.
     */
    private val grids = LinkedList<Grid>()

    /**
     * контроллер который управляет передвижением объекта
     */
    private var moveController: MoveController? = null

    override suspend fun processMessage(msg: Any) {
        logger.debug("MovingObject processMessage ${msg.javaClass.simpleName}")

        when (msg) {
            is MovingObjectMsg.UpdateMove -> updateMove()
            is MovingObjectMsg.LoadGrids -> {
                loadGrids()
                msg.job?.complete()
            }
            is MovingObjectMsg.UnloadGrids -> {
                unloadGrids()
                msg.job?.complete()
            }
            else -> super.processMessage(msg)
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
    protected suspend fun unloadGrids() {
        if (grids.isEmpty()) {
            throw RuntimeException("unloadGrids - grids is empty")
        }

        if (this is Human) grids.forEach { _ ->
            grid.sendJob(Deactivate(this, Job())).join()
        }
        grids.clear()
    }

    /**
     * начать движение объекта
     */
    suspend fun startMove(controller: MoveController) {
        if (controller.canStartMoving()) {
            moveController?.stop()
            moveController = controller
            controller.start()
        } else {
            logger.debug("cant start move $this")
        }
    }

    /**
     * обработка движения от TimeController
     */
    private suspend fun updateMove() {
        val result = moveController?.updateAndResult()
        if (result != null && result) {
            TimeController.instance.deleteMovingObject(this)
        }
    }

    override suspend fun remove() {
        moveController?.stop()
        super.remove()
    }

    /**
     * сохранить позицию объекта в базу (вызывается периодически в движении)
     */
    abstract fun storePositionInDb()

    /**
     * текущий режим перемещения объекта
     */
    protected open fun getMovementMode(): MoveMode {
        return MoveMode.WALK
    }

    /**
     * текущий тип движения (идем, плывем и тд)
     */
    fun getMovementType(): MoveType {
        // TODO проверить тайл подо мной, если это вода то SWIM иначе WALK
        return MoveType.WALK
    }

    /**
     * текущая скорость передвижения (используется при вычислении перемещения за единицу времени)
     * тут надо учитывать статы и текущий режим перемещения
     * сколько игровых координат проходим за 1 реальную секунду
     */
    fun getMovementSpeed(): Double {
        // TODO учесть пересечение воды
        return when (getMovementMode()) {
            MoveMode.STEAL -> 50.0
            MoveMode.WALK -> 100.0
            MoveMode.RUN -> 160.0
        }
    }

    /**
     * изменился грид в котором находимся. надо отреагировать
     */
    fun onGridChanged() {
    }

    fun onLeaveGrid() {
    }
}