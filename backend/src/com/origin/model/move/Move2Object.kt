package com.origin.model.move

import com.origin.model.GameObject
import com.origin.model.MovingObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * движение к объекту и взаимодействие с ним если дошли
 */
@ObsoleteCoroutinesApi
class Move2Object(me: MovingObject, target: GameObject) : MoveController(me) {
    override suspend fun canStartMoving(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun implementation(deltaTime: Double): Boolean {
        TODO("Not yet implemented")
    }
}