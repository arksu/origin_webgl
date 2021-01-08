package com.origin.model.move

import com.origin.model.MovingObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * движение объекта к заданной точке на карте
 */
@ObsoleteCoroutinesApi
class Move2Point(target: MovingObject, val toX: Int, val toY: Int, val type: MoveType = MoveType.WALK) :
    MoveController(target) {

    override fun canStartMoving(): Boolean {
//        TODO("Not yet implemented")
        return true
    }

    override fun implementation(deltaTime: Double): Boolean {
        TODO("Not yet implemented")
    }
}