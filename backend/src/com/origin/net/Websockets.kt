package com.origin.net

import com.google.gson.Gson
import com.origin.ServerConfig.PROTO_VERSION
import com.origin.net.model.GameRequest
import com.origin.net.model.GameResponse
import com.origin.net.model.GameSession
import com.origin.utils.MapDeserializerDoubleAsIntFix.gsonDeserializer
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

val gsonSerializer = Gson()

fun Route.websockets() {

    val welcomeMessage = GameResponse()
    welcomeMessage.channel = "general"
    welcomeMessage.data = "welcome to Origin $PROTO_VERSION"
    welcomeMessage.id = 0

    webSocket("/game") {
        val session = GameSession(this)
        gameSessions += session
        logger.debug("ws connected")

        outgoing.send(Frame.Text(gsonSerializer.toJson(welcomeMessage)))

        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        logger.debug("RECV: $text")

                        val req = gsonDeserializer.fromJson(text, GameRequest::class.java)
                        try {
                            session.received(req)
                        } catch (e: Exception) {
                            logger.error("session recv error ${e.message}", e)
                            close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, e.javaClass.simpleName))
                        }
                    }
                    else -> {
                        logger.warn("uncatched frame $frame")
                    }
                }
            }
        } finally {
            logger.debug("ws disconnected")
            gameSessions -= session
        }
    }
}