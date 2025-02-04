package com.origin.move

import com.origin.ObjectID
import com.origin.TimeController
import com.origin.model.BroadcastEvent
import com.origin.model.GameObject
import com.origin.model.MovingObjectMessage
import com.origin.model.ObjectPosition

abstract class MovingObject(id: ObjectID, pos: ObjectPosition) : GameObject(id, pos) {

    /**
     * контроллер который управляет передвижением объекта
     */
    private var moveController: MoveController? = null


    override suspend fun processMessage(msg: Any) {
        when (msg) {
            is MovingObjectMessage.UpdateMove -> onUpdateMove()
            else -> super.processMessage(msg)
        }
    }

    /**
     * начать движение объекта
     */
    open suspend fun startMove(controller: MoveController) {
        logger.debug("startMove")
        val old = moveController
        if (old != null) {
            old.updateAndResult()
            old.stop()
        }
        if (controller.canStartMoving()) {
            moveController = controller
            controller.start()
        } else {
            logger.debug("can't start move {}", this)
        }
    }

    open suspend fun stopMove() {
        logger.warn("stopMove")
        moveController?.stop()
        storePositionInDb()
        moveController = null

        getGridSafety().broadcast(BroadcastEvent.Stopped(this))
    }

    /**
     * обработка движения от TimeController
     */
    private suspend fun onUpdateMove() {
        if (moveController != null) logger.debug("updateMove")
        val result = moveController?.updateAndResult()
        // если контроллера нет. либо он завершил работу
        if (result == null || result == true) {
            TimeController.deleteMovingObject(this)
            moveController = null
        }
    }


    /**
     * удаление объекта
     */
    override suspend fun remove() {
        // TODO
//        moveController?.stop()
        super.remove()
    }

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
        // TODO : смотреть тайл, если мощеный камень - увеличиваем скорость
        val s = when (getMovementMode()) {
            MoveMode.CRAWL -> 18.0 // 1.5 tiles per sec
            MoveMode.WALK -> 36.0 // 3.0 tiles per sec
            MoveMode.RUN -> 54.0 // 4.5 tiles per sec
            MoveMode.SPRINT -> 72.0 // 6.0 tiles per sec
        }
        // по воде движемся в 2 раза медленее
        return if (getMovementType() == MoveType.SWIMMING) s / 2 else s
    }
}