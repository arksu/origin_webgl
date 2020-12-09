package com.origin.net

import com.google.gson.Gson
import com.origin.ServerConfig.PROTO_VERSION
import com.origin.net.model.GameSession
import com.origin.net.model.WSResponse
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import java.time.Duration
import java.util.*
import kotlin.collections.LinkedHashSet

fun WebSockets.WebSocketOptions.websockets() {
    // websockets config options
    pingPeriod = Duration.ofSeconds(15)
}

/**
 * список игровых коннектов к серверу
 */
val gameSessions = Collections.synchronizedSet(LinkedHashSet<GameSession>())

fun Route.websockets() {
    val gson = Gson()

    val welcomeMessage = WSResponse()
    welcomeMessage.channel = "general"
    welcomeMessage.data = "welcome to Origin $PROTO_VERSION"
    welcomeMessage.id = 0

    webSocket("/game") {
        val session = GameSession(this)
        gameSessions += session
        logger.debug("ws connected")

        /*
         val player = Player(character, session!!)
        if (!World.instance.spawnPlayer(player)) {
            throw GameException("player could not be spawned")
        }
         */

        outgoing.send(Frame.Text(gson.toJson(welcomeMessage)))

        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        outgoing.send(Frame.Pong(frame.buffer))
                        val text = frame.readText()
                        logger.debug("RECV: $text")

                        val r = WSResponse()
                        r.id = 1;
                        r.data = "ok"
                        outgoing.send(Frame.Text(gson.toJson(r)))
                        
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
            gameSessions -= session
        }
    }
}