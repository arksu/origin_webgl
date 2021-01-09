package com.origin.model.move

import com.origin.model.MovingObject
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * движение объекта к заданной точке на карте
 */
@ObsoleteCoroutinesApi
class Move2Point(target: MovingObject, val toX: Int, val toY: Int, val startMode: MoveMode) :
    MoveController(target) {

    override fun canStartMoving(): Boolean {
//        TODO("Not yet implemented")
        return true
    }

    override fun implementation(deltaTime: Double): Boolean {
        val tdx = (toX - x).toDouble()
        val tdy = (toY - y).toDouble()

        // расстояние оставшееся до конечной точки
        val td = sqrt(tdx.pow(2) + tdy.pow(2))

        // сколько прошли: либо расстояние пройденное за тик, либо оставшееся до конечной точки. что меньше
        val distance = (deltaTime * target.getMovementSpeed()).coerceAtMost(td)

        // помножим расстояние которое должны пройти на единичный вектор
        val dx = x + (tdx / td) * distance
        val dy = y + (tdy / td) * distance

        // TODO process
        target.pos.grid

        return false
    }
}