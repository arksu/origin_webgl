package com.origin.model.move

import com.origin.collision.CollisionResult
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
class Move2Point(target: MovingObject, private val toX: Int, private val toY: Int, val startMode: MoveMode) :
    MoveController(target) {

    override suspend fun canStartMoving(): Boolean {
        val (nx, ny) = getNewPoint(0.1)

        val c = checkCollision(nx.roundToInt(), ny.roundToInt(), null)

        return c.result == CollisionResult.CollisionType.COLLISION_NONE
    }

    override suspend fun implementation(deltaTime: Double): Boolean {
        val (nx, ny) = getNewPoint(deltaTime)

        // TODO process
        val c = checkCollision(nx.roundToInt(), ny.roundToInt(), null)

        when (c.result) {
            CollisionResult.CollisionType.COLLISION_NONE -> {
                target.pos.setXY(nx, ny)

                // TODO Broadcast
                target.pos.grid.send(GridMsg.Broadcast())

                if (target is Human) {
                    target.updateVisibleObjects(false)
                }
            }
            else -> {

            }
        }

        return false
    }

    private fun getNewPoint(deltaTime: Double): Pair<Double, Double> {
        val tdx = (toX - x).toDouble()
        val tdy = (toY - y).toDouble()

        // расстояние оставшееся до конечной точки
        val td = sqrt(tdx.pow(2) + tdy.pow(2))

        // сколько прошли: либо расстояние пройденное за тик, либо оставшееся до конечной точки. что меньше
        val distance = (deltaTime * target.getMovementSpeed()).coerceAtMost(td)

        // помножим расстояние которое должны пройти на единичный вектор
        return Pair(x + (tdx / td) * distance, y + (tdy / td) * distance)
    }
}