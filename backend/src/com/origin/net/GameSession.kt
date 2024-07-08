package com.origin.net

import com.google.gson.Gson
import com.origin.config.ServerConfig
import com.origin.jooq.tables.records.AccountRecord
import com.origin.jooq.tables.records.CharacterRecord
import com.origin.model.GameObjectMessage
import com.origin.model.Player
import com.origin.model.PlayerMessage
import com.origin.model.SpawnType.*
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
        logger.debug("client request {}", request)
    }

    suspend fun connected(request: GameRequestDTO) {
        ack(request, AuthorizeTokenResponse(character.id, ServerConfig.PROTO_VERSION))

        player = Player(character, this)

        // пробуем заспавнить игрока в мир
        val spawnResult = player.sendAndWaitAck(GameObjectMessage.Spawn(listOf(EXACTLY_POINT, NEAR, RANDOM_SAME_REGION)))
        if (!spawnResult) throw RuntimeException("Failed to spawn player")

        player.send(PlayerMessage.Connected())
    }

    suspend fun disconnected() {
        isDisconnected = true
        player.send(PlayerMessage.Disconnected())
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