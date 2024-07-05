package com.origin.model

import com.origin.util.MessageWithAck

sealed class GameObjectMessage {
    class Spawn(val variants: List<SpawnType>) : MessageWithAck<Boolean>()
}
