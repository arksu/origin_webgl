package com.origin.net.model

import com.origin.ServerConfig
import com.origin.entity.Account
import com.origin.entity.Character
import com.origin.entity.Characters
import com.origin.model.GameObjectMsg.Spawn
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
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger(GameSession::class.java)

/**
 * игровая сессия (коннект)
 */
@ObsoleteCoroutinesApi
class GameSession(private val connect: DefaultWebSocketSession) {
    var ssid: String? = null
        private set

    private var account: Account? = null

    private lateinit var player: Player

    var isDisconnected = false

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
                    Character.find { Characters.account eq account!!.id and Characters.id.eq(selectedCharacterId) }
                        .singleOrNull()
                        ?: throw BadRequest("character not found")
                }
                // создали игрока, его позицию
                val player = Player(character, this)

                // спавним игрока в мир, прогружаются гриды, активируются
                val resp = CompletableDeferred<Boolean>()
                player.send(Spawn(resp))

                if (!resp.await()) {
                    throw BadRequest("failed spawn player into world")
                }

                this.player = player
                player.send(PlayerMsg.Connected())

                ack(r, "welcome to Origin ${ServerConfig.PROTO_VERSION}")
            }
        } else {
            when (r.target) {
                "mapclick" -> {
                    val x = (r.data["x"] as Long?) ?: throw BadRequest("wrong coord x")
                    val y = (r.data["y"] as Long?) ?: throw BadRequest("wrong coord y")
                    player.send(PlayerMsg.MapClick(x.toInt(), y.toInt()))
                }
                else -> {
                    logger.warn("unknown target ${r.target}")
                }
            }
        }
    }

    suspend fun disconnected() {
        isDisconnected = true
        logger.warn("disconnected")
        player.send(PlayerMsg.Disconnected())
    }

    /**
     * ответ на запрос клиента
     */
    private suspend inline fun ack(req: GameRequest, d: Any? = null) {
        send(GameResponse(req.id, d))
    }

    private suspend fun send(r: GameResponse) {
        if (!isDisconnected) {
//            logger.debug("send $r")
            connect.outgoing.send(Frame.Text(gsonSerializer.toJson(r)))
        }
    }

    suspend fun send(m: ClientMessage) {
        send(GameResponse(m.channel, m))
    }

    suspend fun kick() {
        logger.warn("kick")
        connect.close(CloseReason(CloseReason.Codes.NORMAL, "kicked"))
//        player.send(PlayerMsg.Disconnected())
    }
}