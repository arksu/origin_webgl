package com.origin.model.move

import com.origin.ServerConfig
import com.origin.model.MovingObject
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.math.pow

/**
 * реализует передвижения объектов
 * расчитывает новую позицию. ставит ее объекту и уведомляет всех о смене позиции
 */
@ObsoleteCoroutinesApi
abstract class MoveController(val target: MovingObject) {

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
    abstract fun canStartMoving(): Boolean

    /**
     * внутренняя реализация движения. надо определить куда должны передвинутся за тик
     * @return движение завершено? (истина ежели уперлись во чтото или прибыли в пункт назначения)
     */
    abstract fun implementation(deltaTime: Double): Boolean

    fun start() {
    }

    fun stop() {
    }

    /**
     * обработать тик передвижения
     * @return движение завершено? (истина ежели уперлись во чтото или прибыли в пункт назначения)
     */
    fun updateAndResult(): Boolean {
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
}