package com.origin.model

import com.origin.move.CheckCollisionModel
import com.origin.move.CollisionResult
import com.origin.util.MessageWithAck
import com.origin.util.MessageWithJob

sealed class GridMessage {
    class Spawn(val obj: GameObject) : MessageWithAck<Boolean>()
    class RemoveObject(val obj: GameObject) : MessageWithJob()
    class Activate(val obj: Human) : MessageWithJob()
    class Deactivate(val obj: Human) : MessageWithJob()

    class CheckCollisionInternal(val model: CheckCollisionModel) : MessageWithAck<CollisionResult>()
}