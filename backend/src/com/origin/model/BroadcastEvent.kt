package com.origin.model

import com.origin.move.MoveType
import com.origin.net.ChatChannel

sealed class BroadcastEvent {
    class Moved(
        val obj: GameObject,
        val toX: Int,
        val toY: Int,
        val speed: Double,
        val moveType: MoveType,
    ) : BroadcastEvent()

    class StartMove(
        val obj: GameObject,
        val toX: Int,
        val toY: Int,
        val speed: Double,
        val moveType: MoveType,
    ) : BroadcastEvent()

    class Stopped(val obj: GameObject) : BroadcastEvent()
    class ChatMessage(val obj: GameObject, val channel: ChatChannel, val text: String) : BroadcastEvent()
}