package com.origin.model.move

import com.origin.TimeController
import com.origin.collision.CollisionResult
import com.origin.model.BroadcastEvent
import com.origin.model.Human
import com.origin.model.MovingObject
import com.origin.net.logger
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * движение объекта к заданной точке на карте
 */
@ObsoleteCoroutinesApi
class Move2Point(me: MovingObject, private val toX: Int, private val toY: Int) : MoveController(me) {

    override suspend fun canStartMoving(): Boolean {
        // берем новую точку через 1 тик
        // чтобы убедиться что мы можем туда передвигаться
        val (nx, ny) = calcNewPoint(1.0 / TimeController.TICKS_PER_SECOND, me.getMovementSpeed())

        logger.debug("nx=$nx ny=$ny")

        // проверим коллизию с этой новой точкой
        val c = checkCollision(nx.roundToInt(), ny.roundToInt(), null, false)

        // можем двигаться только если коллизии нет
        return c.result == CollisionResult.CollisionType.COLLISION_NONE
    }

    override suspend fun start() {
        super.start()

        // в самом начале движения пошлем пакет о том что объект уже начал движение
        me.pos.grid.broadcast(BroadcastEvent.StartMove(
            me, toX, toY, me.getMovementSpeed(), me.getMovementType()
        ))
    }

    override suspend fun implementation(deltaTime: Double): Boolean {
        // запомним тип движеня на начало обсчетов. возможно он изменится после
        val moveType = me.getMovementType()
        // также запомним скорость с которой шли
        val speed = me.getMovementSpeed()
        // очередная точка на пути
        val (nx, ny) = calcNewPoint(deltaTime, speed)

        // проверим коллизию при движении в новую точку
        val nxi = nx.roundToInt()
        val nyi = ny.roundToInt()
        val c = checkCollision(nxi, nyi, null, true)

        when (c.result) {
            CollisionResult.CollisionType.COLLISION_NONE -> {

                // сколько осталось идти до конечной точки
                val left = sqrt((toX - x).toDouble().pow(2) + (toY - y).toDouble().pow(2))
                // расстояние до конечной точки при котором считаем что уже дошли куда надо
                return if (left <= 1.0) {
                    me.stopMove()
                    true
                } else {
                    if (me is Human) {
                        me.updateVisibleObjects(false)
                    }
                    me.pos.grid.broadcast(BroadcastEvent.Moved(
                        me, toX, toY, speed, moveType
                    ))
                    false
                }
            }
            CollisionResult.CollisionType.COLLISION_FAIL -> {
                // ошибка при обработке коллизии. надо остановить объект и удалить контроллер
                me.stopMove()
                return true
            }
            else -> {
                // коллизия с чем то. надо остановить работу и обработать результат
                me.stopMove()
                return true
            }
        }
    }

    private fun calcNewPoint(deltaTime: Double, speed: Double): Pair<Double, Double> {
        val tdx = (toX - x).toDouble()
        val tdy = (toY - y).toDouble()

        // расстояние оставшееся до конечной точки
        val td = sqrt(tdx.pow(2) + tdy.pow(2))

        // сколько прошли: либо расстояние пройденное за тик, либо оставшееся до конечной точки. что меньше
        val distance = (deltaTime * speed).coerceAtMost(td)

        // помножим расстояние которое должны пройти на единичный вектор
        return Pair(x + (tdx / td) * distance, y + (tdy / td) * distance)
    }
}