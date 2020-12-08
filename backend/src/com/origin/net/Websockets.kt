package com.origin.net

import com.origin.ServerConfig.PROTO_VERSION
import com.origin.net.model.GameSession
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import java.util.*
import kotlin.collections.LinkedHashSet

fun WebSockets.WebSocketOptions.websockets() {
    // websockets config options
}

/**
 * список игровых коннектов к серверу
 */
val gameSessions = Collections.synchronizedSet(LinkedHashSet<GameSession>())



fun Route.websockets() {
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
        outgoing.send(Frame.Text("welcome to origin $PROTO_VERSION"))
//        outgoing.send(Test("ddd"))

        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        outgoing.send(Frame.Pong(frame.buffer))
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
            gameSessions -= session
        }
    }
}