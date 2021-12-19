package com.origin.net

import com.google.gson.Gson
import com.origin.net.model.GameRequest
import com.origin.net.model.GameSession
import com.origin.utils.MapDeserializerDoubleAsIntFix.gsonDeserializer
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.time.Duration
import java.util.*

fun WebSockets.WebSocketOptions.websockets() {
    // websockets config options
    pingPeriod = Duration.ofSeconds(15)
}

/**
 * список игровых коннектов к серверу
 */
@ObsoleteCoroutinesApi
val gameSessions: MutableSet<GameSession> = Collections.synchronizedSet(LinkedHashSet())

/**
 * для сериализации пакетов (отправка клиенту)
 */
val gsonSerializer = Gson()

@ObsoleteCoroutinesApi
fun Route.websockets() {

    webSocket("/api/game") {
        val session = GameSession(this)
        gameSessions += session
        logger.debug("ws connected $this.")

        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        logger.warn("RECV: $text")

                        val req = gsonDeserializer.fromJson(text, GameRequest::class.java)
                        try {
                            session.received(req)
                        } catch (e: Exception) {
                            logger.error("session recv error ${e.message}", e)
                            close(
                                CloseReason(
                                    CloseReason.Codes.INTERNAL_ERROR,
                                    e.javaClass.simpleName + ": " + e.message
                                )
                            )
                        }
                    }
                    else -> {
                        logger.warn("uncatched frame $frame")
                    }
                }
            }
        } finally {
            logger.debug("ws disconnected")
            session.disconnected()
            gameSessions -= session
        }
    }
}
