package com.origin.net.model

import com.origin.ServerConfig
import com.origin.entity.*
import com.origin.model.BroadcastEvent
import com.origin.model.BroadcastEvent.ChatMessage.Companion.GENERAL
import com.origin.model.GameObjectMsg
import com.origin.model.Player
import com.origin.model.PlayerMsg
import com.origin.net.api.AuthorizationException
import com.origin.net.api.BadRequest
import com.origin.net.gameSessions
import com.origin.net.gsonSerializer
import com.origin.utils.ObjectID
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * ответ на авторизацию по токену
 */
data class AuthorizeTokenResponse(
    val characterId: ObjectID,
    val proto: String
)

/**
 * игровая сессия (коннект)
 */
@ObsoleteCoroutinesApi
class GameSession(private val connect: DefaultWebSocketSession) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(GameSession::class.java)
    }

    @Volatile
    private var authorized: Boolean = false

    private var account: Account? = null

    private lateinit var player: Player

    private var isDisconnected = false

    suspend fun received(r: GameRequest) {
        // инициализация сессии
        if (!authorized) {
            // начальная точка входа клиента в игру (авторизация по ssid)
            // также передается выбранный персонаж
            if (r.target == "token") {
                // получим токен из запроса
                val token = (r.data["token"] as String?) ?: throw BadRequest("wrong token")

                // найдем наш аккаунт по токену
                val acc = transaction {
                    val a = Account.find { Accounts.wsToken eq token }.singleOrNull()
                        ?: throw AuthorizationException("Wrong token")
                    // токен можно использовать только 1 раз, поэтому зануляем его
                    a.wsToken = null
                    a
                }
                authorized = true
                account = acc

                // загрузим выбранного персонажа
                val character = transaction {
                    val c =
                        Character.find { Characters.account eq acc.id and Characters.id.eq(acc.selectedCharacter) }
                            .singleOrNull()
                            ?: throw BadRequest("character not found")
//                    c.lastLogged = Timestamp(Date().time)
                    c
                }

                // кикнуть таких же персонажей этого юзера
                // (можно заходить в игру своими разными персонажами одновременно)
                gameSessions.forEach { s ->
                    if (s.authorized && s::player.isInitialized && s.player.id == character.id.value) {
                        runBlocking {
                            s.kick()
                        }
                    }
                }
                ack(r, AuthorizeTokenResponse(character.id.value, ServerConfig.PROTO_VERSION))

                // создали игрока, его позицию
                val player = Player(character, this)
                this.player = player

                // спавним игрока в мир, прогружаются гриды, активируются
                val resp = CompletableDeferred<Boolean>()
                // SPAWN at the same position
                player.send(GameObjectMsg.Spawn(resp))

                if (!resp.await()) {
                    val resp2 = CompletableDeferred<Boolean>()
                    // SPAWN NEAR
                    player.send(GameObjectMsg.SpawnNear(resp2))
                    if (!resp2.await()) {
                        val resp3 = CompletableDeferred<Boolean>()
                        // SPAWN RANDOM
                        player.send(GameObjectMsg.SpawnRandom(resp3))
                        if (!resp3.await()) {
                            throw RuntimeException("failed spawn player into world")
                        }
                    }
                }

                player.send(PlayerMsg.Connected())
            }
        } else {
            when (r.target) {
                "mapclick" -> {
                    val x = (r.data["x"] as Long?) ?: throw BadRequest("wrong coord x")
                    val y = (r.data["y"] as Long?) ?: throw BadRequest("wrong coord y")
                    val btn = (r.data["b"] as Long?) ?: throw BadRequest("wrong button")
                    val flags = (r.data["f"] as Long?) ?: throw BadRequest("wrong flags")
                    player.send(PlayerMsg.MapClick(getClientButton(btn), flags.toInt(), x.toInt(), y.toInt()))
                }
                // клик по объекту
                "objclick" -> {
                    val id = (r.data["id"] as Long?) ?: throw BadRequest("wrong obj id")
                    val x = (r.data["x"] as Long?) ?: throw BadRequest("wrong coord x")
                    val y = (r.data["y"] as Long?) ?: throw BadRequest("wrong coord y")
                    val flags = (r.data["f"] as Long?) ?: throw BadRequest("wrong flags")
                    player.send(PlayerMsg.ObjectClick(id, flags.toInt(), x.toInt(), y.toInt()))
                }
                // right click
                "objrclick" -> {
                    val id = (r.data["id"] as Long?) ?: throw BadRequest("wrong obj id")
                    player.send(PlayerMsg.ObjectRightClick(id))
                }
                // context menu item selected
                "cmselect" -> {
                    val item = (r.data["item"] as String?) ?: throw BadRequest("no item")
                    player.send(PlayerMsg.ContextMenuItem(item))
                }
                // клик по вещи в открытом инвентаре
                "itemclick" -> {
                    val id = (r.data["id"] as Long?) ?: throw BadRequest("wrong obj id")
                    val inventoryId = (r.data["iid"] as Long?) ?: throw BadRequest("wrong obj id")
                    val x = (r.data["x"] as Long?) ?: throw BadRequest("wrong coord x")
                    val y = (r.data["y"] as Long?) ?: throw BadRequest("wrong coord y")
                    val ox = (r.data["ox"] as Long?) ?: throw BadRequest("wrong coord ox")
                    val oy = (r.data["oy"] as Long?) ?: throw BadRequest("wrong coord oy")
                    player.send(PlayerMsg.ItemClick(id, inventoryId, x.toInt(), y.toInt(), ox.toInt(), oy.toInt()))
                }
                // закрыть инвентарь
                "invclose" -> {
                    val inventoryId = (r.data["iid"] as Long?) ?: throw BadRequest("wrong obj id")
                    player.send(PlayerMsg.InventoryClose(inventoryId))
                }
                "chat" -> {
                    val text = (r.data["text"] as String?) ?: throw BadRequest("no text")
                    if (text.isNotEmpty()) {
                        transaction {
                            ChatHistory.insert {
                                it[owner] = player.id
                                it[channel] = GENERAL.toByte()
                                it[ChatHistory.text] = text
                            }
                        }
                        if (text.startsWith("/")) {
                            // удаляем слеш в начале строки
                            player.consoleCommand(text.substring(1))
                        } else {
                            player.grid.broadcast(BroadcastEvent.ChatMessage(player, GENERAL, text))
                        }
                    }
                }
                else -> {
                    logger.warn("unknown target ${r.target}")
                }
            }
        }
    }

    suspend fun disconnected() {
        if (!isDisconnected) {
            isDisconnected = true
            logger.warn("disconnected")

            if (::player.isInitialized) {
                player.send(PlayerMsg.Disconnected())
            }
        }
    }

    /**
     * ответ на запрос клиента
     */
    private suspend inline fun ack(req: GameRequest, d: Any? = null) {
        send(GameResponse(req.id, d))
    }

    private suspend fun send(r: GameResponse) {
        if (!isDisconnected) connect.outgoing.send(Frame.Text(gsonSerializer.toJson(r)))
    }

    suspend fun send(m: ServerMessage) {
        send(GameResponse(m.channel, m))
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
