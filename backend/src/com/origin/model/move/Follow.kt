package com.origin.model.move

import com.origin.model.GameObject
import com.origin.model.MovingObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * постоянное следование за объектом "на привязке" на определенной дистанции
 */
@ObsoleteCoroutinesApi
class Follow(me: MovingObject, target: GameObject, followDistance: Int) : MoveController(me) {

    override val toX: Int
        get() = TODO("Not yet implemented")
    override val toY: Int
        get() = TODO("Not yet implemented")

    override suspend fun implementation(c: CollisionResult, left: Double, speed: Double, moveType: MoveType): Boolean {
        TODO("Not yet implemented")
    }

}