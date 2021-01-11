package com.origin.model.move

import com.origin.TimeController
import com.origin.collision.CollisionResult
import com.origin.model.BroadcastEvent
import com.origin.model.GridMsg
import com.origin.model.Human
import com.origin.model.MovingObject
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
        val (nx, ny) = calcNewPoint(1000.0 / TimeController.TICKS_PER_SECOND, me.getMovementSpeed())

        // проверим коллизию с этой новой точкой
        val c = checkCollision(nx.roundToInt(), ny.roundToInt(), null, false)

        // можем двигаться только если коллизии нет
        return c.result == CollisionResult.CollisionType.COLLISION_NONE
    }

    override suspend fun implementation(deltaTime: Double): Boolean {
        val moveType = me.getMovementType()
        val speed = me.getMovementSpeed()
        val (nx, ny) = calcNewPoint(deltaTime, speed)

        // проверим коллизию при движении в новую точку
        val nxi = nx.roundToInt()
        val nyi = ny.roundToInt()
        val c = checkCollision(nxi, nyi, null, true)

        when (c.result) {
            CollisionResult.CollisionType.COLLISION_NONE -> {
                me.pos.grid.send(GridMsg.Broadcast(BroadcastEvent.Moved(
                    me, nxi, nyi, speed, me.pos.heading, moveType
                )))

                if (me is Human) {
                    me.updateVisibleObjects(false)
                }
            }
            CollisionResult.CollisionType.COLLISION_FAIL -> {

            }
            else -> {

            }
        }

        return false
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