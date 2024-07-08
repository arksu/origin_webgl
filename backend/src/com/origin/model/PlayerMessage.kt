package com.origin.model

import com.origin.ObjectID

sealed class PlayerMessage {
    class Connected
    class Disconnected
    class ObjectRightClick(val id: ObjectID)
}