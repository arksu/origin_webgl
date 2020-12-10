package com.origin.net.model

import com.origin.entity.Account
import com.origin.net.GameServer
import com.origin.net.api.AuthorizationException
import com.origin.net.api.BadRequest
import com.origin.net.gsonSerializer
import com.origin.net.logger
import io.ktor.http.cio.websocket.*

/**
 * игровая сессия (коннект)
 */
class GameSession(private val connect: DefaultWebSocketSession) {
    var ssid: String? = null
        private set

    private var account: Account? = null

    suspend fun received(r: GameRequest) {
        // инициализация сессии
        if (ssid == null) {
            if (r.target == "ssid") {
                // установим ssid
                ssid = (r.data["ssid"] as String?) ?: throw BadRequest("wrong ssid")
                // и найдем наш аккаунт в кэше
                account = GameServer.accountCache.get(ssid) ?: throw AuthorizationException()
            }
        } else {
            when (r.target) {
                "gameEnter" -> {
                    val selectedCharacterId: Number = r.data["selectedCharacterId"] as Number
                    println(selectedCharacterId)
                }
                "test" -> {
                    ack(r, "test")
                }
                "bye" -> {
                    // TEST
                    connect.close(CloseReason(CloseReason.Codes.NORMAL, "said bye"))
                }
                else -> {
                    logger.warn("unknown target ${r.target}")
                }
            }
        }
    }

    /**
     * ответ на запрос клиента
     */
    private suspend fun ack(req: GameRequest, d: Any? = null) {
        val response = GameResponse()
        response.id = req.id
        response.data = d
        connect.outgoing.send(Frame.Text(gsonSerializer.toJson(response)))
    }

    suspend fun kick() {
        logger.warn("kick")
        connect.close(CloseReason(CloseReason.Codes.NORMAL, "kicked"))
    }
}