package com.origin.net

import com.google.gson.Gson
import com.origin.config.DatabaseConfig
import com.origin.config.ServerConfig
import com.origin.error.BadRequestException
import com.origin.jooq.tables.records.AccountRecord
import com.origin.jooq.tables.records.CharacterRecord
import com.origin.jooq.tables.references.CHAT_HISTORY
import com.origin.model.GameObjectMessage
import com.origin.model.Player
import com.origin.model.PlayerMessage
import com.origin.model.SpawnType.*
import com.origin.net.ClientPacket.*
import com.origin.util.getClientButton
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
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


    suspend fun process(request: GameRequestDTO) {
        logger.debug("client request {}", request)

        when (request.target) {
            MAP_CLICK.n -> {
                val x = (request.data["x"] as Long?) ?: throw BadRequestException("wrong coord x")
                val y = (request.data["y"] as Long?) ?: throw BadRequestException("wrong coord y")
                val btn = (request.data["b"] as Long?) ?: throw BadRequestException("wrong button")
                val flags = (request.data["f"] as Long?) ?: throw BadRequestException("wrong flags")
                player.send(PlayerMessage.MapClick(getClientButton(btn), flags.toInt(), x.toInt(), y.toInt()))
            }

            OBJECT_CLICK.n -> {
                val id = (request.data["id"] as Long?) ?: throw BadRequestException("wrong obj id")
                val x = (request.data["x"] as Long?) ?: throw BadRequestException("wrong coord x")
                val y = (request.data["y"] as Long?) ?: throw BadRequestException("wrong coord y")
                val flags = (request.data["f"] as Long?) ?: throw BadRequestException("wrong flags")
                player.send(PlayerMessage.ObjectClick(id, flags.toInt(), x.toInt(), y.toInt()))
            }

            OBJECT_RIGHT_CLICK.n -> {
                val id = (request.data["id"] as Long?) ?: throw BadRequestException("wrong obj id")
                player.send(PlayerMessage.ObjectRightClick(id))
            }

            CHAT.n -> {
                val text = (request.data["text"] as String?) ?: throw BadRequestException("no text")
                if (text.isNotEmpty()) {
                    // обрежем текст до длины поля в бд
                    val trimmed = text.trim()
                    val t = if (trimmed.length > 1020) trimmed.substring(0, 1020) else trimmed
                    runBlocking {
                        DatabaseConfig.dsl
                            .insertInto(CHAT_HISTORY)
                            .set(CHAT_HISTORY.CHANNEL, ChatChannel.GENERAL.id.toByte())
                            .set(CHAT_HISTORY.TEXT, t)
                            .set(CHAT_HISTORY.SENDER_ID, player.id)
                            .set(CHAT_HISTORY.X, player.pos.x.toLong())
                            .set(CHAT_HISTORY.Y, player.pos.y.toLong())
                            .execute()
                    }
                    player.send(PlayerMessage.ChatMessage(t))
                }
            }

            OPEN_MY_INVENTORY.n -> {
                // открывать инвентарь игрока по запросу клиента
                player.inventory.send(player)
            }

            INVENTORY_CLOSE.n -> {
                val inventoryId = (request.data["iid"] as Long?) ?: throw BadRequestException("wrong obj id")
                player.send(PlayerMessage.InventoryClose(inventoryId))

            }

            ITEM_CLICK.n -> {
                val id = (request.data["id"] as Long?) ?: throw BadRequestException("wrong obj id")
                val inventoryId = (request.data["iid"] as Long?) ?: throw BadRequestException("wrong obj id")
                val x = (request.data["x"] as Long?) ?: throw BadRequestException("wrong coord x")
                val y = (request.data["y"] as Long?) ?: throw BadRequestException("wrong coord y")
                val ox = (request.data["ox"] as Long?) ?: throw BadRequestException("wrong coord ox")
                val oy = (request.data["oy"] as Long?) ?: throw BadRequestException("wrong coord oy")
                player.send(PlayerMessage.InventoryItemClick(id, inventoryId, x.toInt(), y.toInt(), ox.toInt(), oy.toInt()))
            }
        }
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