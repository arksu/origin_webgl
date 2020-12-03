package com.origin.net

import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import java.time.Duration
import java.util.*
import kotlin.collections.LinkedHashSet

fun WebSockets.WebSocketOptions.websockets() {
    pingPeriod = Duration.ofSeconds(20)
}

val wsConnections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())

fun Route.websockets() {
    webSocket("/ws") {
        wsConnections += this
        println("onConnect")
        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()

                        if (text.equals("bye", ignoreCase = true)) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "said bye"))
                        }
                    }
                    else -> {
                        println("uncatched frame $frame")
                    }
                }
            }
        } finally {
            wsConnections -= this
        }
    }
}
/*
    /**
     * выбрать игровой персонаж
     */
    @Throws(GameException::class)
    fun selectCharacter(session: GameSession?, data: Map<String, Any>): Any {
        val character = Database.em().findById(
            Character::class.java, Math.toIntExact(
                (data["id"] as Long?)!!
            )
        ) ?: throw GameException("no such player")
        val player = Player(character, session!!)
        if (!World.instance.spawnPlayer(player)) {
            throw GameException("player could not be spawned")
        }
        return character
    }
 */