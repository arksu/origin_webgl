package com.origin.model

import com.origin.move.CheckCollisionModel
import com.origin.move.CollisionResult
import com.origin.util.MessageWithAck

sealed class GridMessage {
    class Spawn(val obj: GameObject) : MessageWithAck<Boolean>()

    class CheckCollisionInternal(model: CheckCollisionModel) : MessageWithAck<CollisionResult>()
}