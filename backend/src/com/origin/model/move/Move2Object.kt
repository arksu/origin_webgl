package com.origin.model.move

import com.origin.model.MovingObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class Move2Object(target: MovingObject) : MoveController(target) {
    override suspend fun canStartMoving(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun implementation(deltaTime: Double): Boolean {
        TODO("Not yet implemented")
    }
}