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
abstract class MoveController(val target: MovingObject) {

    val x
        get() = target.pos.x

    val y
        get() = target.pos.y

    /**
     * последняя сохраненная в базу позиция
     */
    private var storedX: Double = target.pos.x.toDouble()
    private var storedY: Double = target.pos.y.toDouble()

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

    fun start() {
        TimeController.instance.addMovingObject(target)
    }

    fun stop() {
        TimeController.instance.deleteMovingObject(target)
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
                val dx: Double = target.pos.x - storedY
                val dy: Double = target.pos.y - storedY

                // если передвинулись достаточно далеко
                if (dx.pow(2) + dy.pow(2) > ServerConfig.UPDATE_DB_DISTANCE.toDouble().pow(2)) {
                    target.storePositionInDb()
                    storedX = target.pos.x.toDouble()
                    storedY = target.pos.y.toDouble()
                }
            } else {
                // движение завершено. внутри implementation сохранили позицию в базе, запомним и тут
                storedX = target.pos.x.toDouble()
                storedY = target.pos.y.toDouble()
            }
            lastMoveTime = currentTime
            return result
        }
        return false
    }

    protected suspend fun checkCollision(toX: Int, toY: Int, virtual: GameObject?): CollisionResult {
        // шлем сообщение гриду о необходимости проверить коллизию
        val resp = CompletableDeferred<CollisionResult>()
        target.pos.grid.send(GridMsg.CheckCollision(target, toX, toY, target.getMovementType(), virtual, resp))
        return resp.await()
    }
}