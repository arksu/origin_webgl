package com.origin.model

import com.origin.move.CheckCollisionModel
import com.origin.move.CollisionResult
import com.origin.move.MoveType
import com.origin.util.MessageWithAck
import com.origin.util.MessageWithJob

sealed class GridMessage {
    class Update
    class Spawn(val obj: GameObject) : MessageWithAck<Boolean>()
    class RemoveObject(val obj: GameObject) : MessageWithJob()
    class Activate(val obj: Human) : MessageWithJob()
    class Deactivate(val obj: Human) : MessageWithJob()
    class CheckCollision(
        val obj: GameObject,
        val toX: Int,
        val toY: Int,
        val dist: Double,
        val type: MoveType,
        val virtual: GameObject?,
        val isMove: Boolean,
    ) : MessageWithAck<CollisionResult>()

    class Broadcast(val e: BroadcastEvent)

    class CheckCollisionInternal(val model: CheckCollisionModel) : MessageWithAck<CollisionResult>()
}