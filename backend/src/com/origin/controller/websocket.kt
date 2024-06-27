package com.origin.controller

import com.google.gson.annotations.SerializedName
import com.origin.GameSession
import com.origin.GameWebServer.gsonDeserializer
import com.origin.GameWebServer.logger
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.jooq.DSLContext
import java.util.*

/**
 * игровой запрос от клиента
 * java NOT kotlin из-за поля data
 * коряво десериализуется если поставить тип котлина Any
 */
data class GameRequestDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("t")
    val target: String,

    @SerializedName("d")
    val data: Map<String, Any>?
)

/**
 * список игровых коннектов к серверу
 */
val gameSessions: MutableSet<GameSession> = Collections.synchronizedSet(LinkedHashSet())

fun Route.websockets(dsl: DSLContext) {

    webSocket("game") {
        for (frame in incoming) {
            // этап авторизации в только что открытом ws коннекте
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    val request = gsonDeserializer.fromJson(text, GameRequestDTO::class.java)
                    logger.debug(request.toString())
                }

                else -> {
                    logger.warn("unknown WS frame $frame")
                }
            }
        }
    }
}