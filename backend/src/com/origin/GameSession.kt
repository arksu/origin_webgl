package com.origin

import com.google.gson.Gson
import com.origin.jooq.tables.records.AccountRecord
import com.origin.jooq.tables.records.CharacterRecord
import com.origin.model.Player
import com.origin.net.AuthorizeTokenResponse
import com.origin.net.GameRequestDTO
import com.origin.net.GameResponseDTO
import com.origin.net.ServerMessage
import io.ktor.websocket.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GameSession(
    private val connect: DefaultWebSocketSession,
    private val token: String,
    val account: AccountRecord,
    val character: CharacterRecord,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(GameSession::class.java)

        /**
         * для сериализации пакетов (отправка клиенту)
         */
        private val gsonSerializer = Gson()
    }

    private lateinit var player: Player

    private var isDisconnected = false


    fun process(request: GameRequestDTO) {
        TODO("Not yet implemented")
    }

    suspend fun connected(request: GameRequestDTO) {
        ack(request, AuthorizeTokenResponse(character.id, ServerConfig.PROTO_VERSION))

        player = Player(character, this)
    }

    fun disconnected() {

    }

    /**
     * ответ на запрос клиента
     */
    private suspend inline fun ack(request: GameRequestDTO, d: Any? = null) {
        send(GameResponseDTO(request.id, d))
    }

    private suspend fun send(response: GameResponseDTO) {
        if (!isDisconnected) connect.outgoing.send(Frame.Text(gsonSerializer.toJson(response)))
    }

    suspend fun send(message: ServerMessage) {
        send(GameResponseDTO(message.channel, message))
    }

    suspend fun kick() {
        logger.warn("kick player")
        isDisconnected = true
        connect.close(CloseReason(CloseReason.Codes.NORMAL, "kicked"))
    }

    suspend fun logout() {
        isDisconnected = true
        connect.close(CloseReason(CloseReason.Codes.NORMAL, "logout"))
    }
}