package com.origin.net

import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import java.time.Duration
import java.util.*
import kotlin.collections.LinkedHashSet

fun WebSockets.WebSocketOptions.websockets() {
    pingPeriod = Duration.ofSeconds(2)
}

val wsConnections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())

fun Route.websockets() {
    webSocket("/game") {
        wsConnections += this
        logger.debug("ws connected")

        /*
         val player = Player(character, session!!)
        if (!World.instance.spawnPlayer(player)) {
            throw GameException("player could not be spawned")
        }
         */

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
            logger.debug("ws disconnected")
            wsConnections -= this
        }
    }
}