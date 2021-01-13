package com.origin.model.move

import com.origin.ServerConfig
import com.origin.TimeController
import com.origin.collision.CollisionResult
import com.origin.model.GameObject
import com.origin.model.GridMsg
import com.origin.model.MovingObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.math.pow

/**
 * реализует передвижения объектов
 * расчитывает новую позицию. ставит ее объекту и уведомляет всех о смене позиции
 */
@ObsoleteCoroutinesApi
abstract class MoveController(val me: MovingObject) {

    val x
        get() = me.pos.x

    val y
        get() = me.pos.y

    protected var lastTime = System.currentTimeMillis()

    /**
     * последняя сохраненная в базу позиция
     */
    private var storedX: Double = me.pos.x.toDouble()
    private var storedY: Double = me.pos.y.toDouble()

    /**
     * время последнего апдейта движения (в системных мсек)
     */
    private var lastMoveTime = System.currentTimeMillis()

    /**
     * возможно ли начать движение
     */
    abstract suspend fun canStartMoving(): Boolean

    /**
     * внутренняя реализация движения. надо определить куда должны передвинутся за тик
     * @param deltaTime сколько времени прошло с последнего апдейта движения (реальные секунды)
     * @return движение завершено? (истина ежели уперлись во чтото или прибыли в пункт назначения)
     */
    abstract suspend fun implementation(deltaTime: Double): Boolean

    /**
     * начать работу контроллера (при начале движения)
     */
    open suspend fun start() {
        TimeController.instance.addMovingObject(me)
    }

    /**
     * остановить работу контроллера (принудительная остановка извне)
     */
    suspend fun stop() {
        TimeController.instance.deleteMovingObject(me)
    }

    /**
     * обработать тик передвижения
     * @return движение завершено? (истина ежели уперлись во чтото или прибыли в пункт назначения)
     */
    suspend fun updateAndResult(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime > lastMoveTime) {
            // узнаем сколько времени прошло между апдейтами
            val deltaTime: Double = (currentTime - lastMoveTime) / 1000.0
            val result = implementation(deltaTime)

            // если движение не завершено - обновляем позицию в базе
            if (!result) {
                val dx: Double = me.pos.x - storedX
                val dy: Double = me.pos.y - storedY
//                logger.debug("move dx=$dx dy=$dy d=${sqrt(dx.pow(2) + dy.pow(2))}")

                // если передвинулись достаточно далеко
                if (dx.pow(2) + dy.pow(2) > ServerConfig.UPDATE_DB_DISTANCE.toDouble().pow(2)) {
                    me.storePositionInDb()
                    storedX = me.pos.x.toDouble()
                    storedY = me.pos.y.toDouble()
                }
            } else {
                // движение завершено. внутри implementation сохранили позицию в базе, запомним и тут
                storedX = me.pos.x.toDouble()
                storedY = me.pos.y.toDouble()
            }
            lastMoveTime = currentTime
            return result
        }
        return false
    }

    /**
     * проверить коллизию и передвинуться через текущий грид
     * всю работу выполняет грид, т.к. объекты для коллизий хранятся только там
     * позицию изменит тоже он если isMove=true
     */
    protected suspend fun checkCollision(toX: Int, toY: Int, virtual: GameObject?, isMove: Boolean): CollisionResult {
        // шлем сообщение гриду о необходимости проверить коллизию
        val resp = CompletableDeferred<CollisionResult>()
        me.pos.grid.send(GridMsg.CheckCollision(me, toX, toY, me.getMovementType(), virtual, isMove, resp))
        return resp.await()
    }
}