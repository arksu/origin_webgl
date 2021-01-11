package com.origin.model.move

import com.origin.model.GameObject
import com.origin.model.MovingObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * постоянное следование за объектом "на привязке" на определенной дистанции
 */
@ObsoleteCoroutinesApi
class Follow(me: MovingObject, target: GameObject, followDitance: Int) : MoveController(me) {
    override suspend fun canStartMoving(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun implementation(deltaTime: Double): Boolean {
        TODO("Not yet implemented")
    }
}