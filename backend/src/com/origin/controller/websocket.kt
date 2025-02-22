package com.origin.controller

import com.origin.GameWebServer.logger
import com.origin.jooq.tables.references.ACCOUNT
import com.origin.jooq.tables.references.CHARACTER
import com.origin.net.GameRequestDTO
import com.origin.net.GameSocket
import com.origin.util.MapDeserializerDoubleAsIntFix.gsonDeserializer
import com.origin.util.transactionResultWrapper
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import java.util.*

/**
 * список открытых игровых коннектов к серверу
 */
val gameSockets: MutableSet<GameSocket> = Collections.synchronizedSet(LinkedHashSet())

fun Route.websockets(dsl: DSLContext) {

    webSocket("game") {
        var session: GameSocket? = null

        logger.debug("ws connected")

        try {
            for (frame in incoming) {
                // этап авторизации в только что открытом ws коннекте
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        val request = gsonDeserializer.fromJson(text, GameRequestDTO::class.java)
                        logger.debug(request.toString())

                        if (request.target == "token") {
                            try {
                                val token = request.data["token"] as String
                                val localSocket = dsl.transactionResultWrapper { trx ->
                                    // ищем аккаунт по токену
                                    val account = trx.selectFrom(ACCOUNT)
                                        .where(ACCOUNT.WS_TOKEN.eq(token))
                                        .fetchOne() ?: throw BadRequestException("Invalid token")

                                    // ищем выбранного персонажа
                                    val character = trx.selectFrom(CHARACTER)
                                        .where(CHARACTER.ID.eq(account.selectedCharacter))
                                        .and(CHARACTER.DELETED.isFalse)
                                        .fetchOne() ?: throw BadRequestException("Character not found")

                                    // создаем игровую сессию
                                    GameSocket(this, token, account, character)
                                }
                                session = localSocket

                                localSocket.connected(request)

                                // добавляем в список активных сессий
                                gameSockets += localSocket
                            } catch (e: Throwable) {
                                logger.error("session process token error ${e.message}", e)
                                close(
                                    CloseReason(
                                        CloseReason.Codes.INTERNAL_ERROR,
                                        e.javaClass.simpleName + ": " + e.message
                                    )
                                )
                            }
                        } else if (session != null) {
                            try {
                                session.process(request)
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
                    }

                    else -> logger.warn("unknown WS frame $frame")
                }
            }
        } finally {
            logger.debug("ws disconnected")
            if (session != null) {
                session.disconnected()
                gameSockets -= session
            }
        }
    }
}