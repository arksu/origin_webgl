package com.origin.net.model

import com.origin.ServerConfig
import com.origin.entity.Account
import com.origin.entity.Character
import com.origin.entity.Characters
import com.origin.entity.ChatHistory
import com.origin.model.BroadcastEvent
import com.origin.model.BroadcastEvent.ChatMessage.Companion.GENERAL
import com.origin.model.GameObjectMsg
import com.origin.model.Player
import com.origin.model.PlayerMsg
import com.origin.net.GameServer
import com.origin.net.api.AuthorizationException
import com.origin.net.api.BadRequest
import com.origin.net.gsonSerializer
import com.origin.utils.ObjectID
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * игровая сессия (коннект)
 */
@ObsoleteCoroutinesApi
class GameSession(private val connect: DefaultWebSocketSession) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(GameSession::class.java)
    }

    var ssid: String? = null
        private set

    private var account: Account? = null

    private lateinit var player: Player

    private var isDisconnected = false

    suspend fun received(r: GameRequest) {
        // инициализация сессии
        if (ssid == null) {
            // начальная точка входа клиента в игру (авторизация по ssid)
            // также передается выбранный персонаж
            if (r.target == "ssid") {
                // установим ssid
                ssid = (r.data["ssid"] as String?) ?: throw BadRequest("wrong ssid")
                // и найдем наш аккаунт в кэше
                account = GameServer.accountCache.get(ssid) ?: throw AuthorizationException()

                // выбраннй перс
                val selectedCharacterId: ObjectID = (r.data["selectedCharacterId"] as ObjectID)

                // load char
                val character = transaction {
                    val c =
                        Character.find { Characters.account eq account!!.id and Characters.id.eq(selectedCharacterId) }
                            .singleOrNull()
                            ?: throw BadRequest("character not found")
                    account!!.selectedCharacter = selectedCharacterId
//                    c.lastLogged = Timestamp(Date().time)
                    c
                }
                // создали игрока, его позицию
                val player = Player(character, this)
                this.player = player

                // спавним игрока в мир, прогружаются гриды, активируются
                val resp = CompletableDeferred<Boolean>()
                player.send(GameObjectMsg.Spawn(resp))

                if (!resp.await()) {
                    val resp2 = CompletableDeferred<Boolean>()
                    player.send(GameObjectMsg.SpawnNear(resp2))
                    if (!resp2.await()) {
                        throw BadRequest("failed spawn player into world")
                    }
                }

                player.send(PlayerMsg.Connected())

                ack(r, "welcome to Origin ${ServerConfig.PROTO_VERSION}")
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
        logger.warn("kick")
        connect.close(CloseReason(CloseReason.Codes.NORMAL, "kicked"))
    }
}