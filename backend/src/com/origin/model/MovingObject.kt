package com.origin.model

import com.origin.TimeController
import com.origin.model.GridMsg.Activate
import com.origin.model.GridMsg.Deactivate
import com.origin.model.move.MoveController
import com.origin.model.move.MoveMode
import com.origin.model.move.MoveType
import com.origin.utils.ObjectID
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

sealed class MovingObjectMsg {
    class UpdateMove
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
    protected val grids = LinkedList<Grid>()

    /**
     * контроллер который управляет передвижением объекта
     */
    private var moveController: MoveController? = null

    /**
     * объект с которым "столкнулись" (прилинковались), может быть виртуальный или реальный
     * если реальный, то при удалении его из known списка должны занулить и здесь.
     * то есть это реальный объект с которым мы взаимодействуем
     */
    private var linkedObject: GameObject? = null

    override suspend fun processMessage(msg: Any) {
//        logger.debug("MovingObject processMessage ${msg.javaClass.simpleName}")

        when (msg) {
            is MovingObjectMsg.UpdateMove -> updateMove()
            else -> super.processMessage(msg)
        }
    }

    /**
     * после спавна сразу загружаем список гридов вокруг
     */
    override suspend fun afterSpawn() {
        loadGrids()
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
                val g = World.getGrid(pos.region, pos.level, gx, gy)
                grids.add(g)

                if (this is Human) {
                    val h = this
                    logger.debug("Activate ${g.x} ${g.y}")
                    g.sendJob(Activate(h, Job())).join()
                }
            }
        }
    }

    /**
     * выгрузить все гриды в которых находимся
     */
    private suspend fun unloadGrids() {
        if (grids.isEmpty()) {
            throw RuntimeException("unloadGrids - grids is empty")
        }

        if (this is Human) grids.forEach {
            logger.debug("Deactivate ${it.x} ${it.y}")
            it.sendJob(Deactivate(this, Job())).join()
        }
        grids.clear()
    }

    /**
     * начать движение объекта
     */
    suspend fun startMove(controller: MoveController) {
        val old = moveController
        if (old != null) {
            old.updateAndResult()
            old.stop()
        }
        if (controller.canStartMoving()) {
            moveController = controller
            controller.start()
        } else {
            logger.debug("cant start move $this")
        }
    }

    suspend fun stopMove() {
        logger.warn("stopMove")
        moveController?.stop()
        storePositionInDb()
        moveController = null

        grid.send(GridMsg.Broadcast(BroadcastEvent.Stopped(this)))
    }

    /**
     * обработка движения от TimeController
     */
    private suspend fun updateMove() {
        val result = moveController?.updateAndResult()
        // если контроллера нет. либо он завершил работу
        if (result == null || result) {
            TimeController.instance.deleteMovingObject(this)
        }
    }

    /**
     * удаление объекта
     */
    override suspend fun remove() {
        // deactivate and unload grids
        unloadGrids()

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
        val s = when (getMovementMode()) {
            MoveMode.STEAL -> 25.0
            MoveMode.WALK -> 40.0
            MoveMode.RUN -> 120.0
        }
        // по воде движемся в 2 раза медленее
        return if (getMovementType() == MoveType.SWIMMING) s / 2 else s
    }

    /**
     * изменился грид в котором находимся. надо отреагировать
     */
    fun onGridChanged() {
    }

    fun onLeaveGrid() {
    }
}