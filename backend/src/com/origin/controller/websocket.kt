package com.origin.controller

import com.google.gson.annotations.SerializedName
import com.origin.GameSession
import com.origin.GameWebServer.gsonDeserializer
import com.origin.GameWebServer.logger
import com.origin.jooq.tables.references.ACCOUNT
import com.origin.jooq.tables.references.CHARACTER
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.impl.DSL
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
        var session: GameSession? = null

        try {
            for (frame in incoming) {
                // этап авторизации в только что открытом ws коннекте
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        val request = gsonDeserializer.fromJson(text, GameRequestDTO::class.java)
                        logger.debug(request.toString())

                        if (request.target == "token") {
                            val token = request.data!!["token"] as String
                            dsl.transactionResult { trx ->
                                val account = trx.dsl().selectFrom(ACCOUNT)
                                    .where(ACCOUNT.WS_TOKEN.eq(token))
                                    .forUpdate()
                                    .fetchOne()

                                if (account != null) {
                                    val character = trx.dsl().selectFrom(CHARACTER)
                                        .where(CHARACTER.ID.eq(account.selectedCharacter))
                                        .and(CHARACTER.DELETED.isFalse)
                                        .fetchOne() ?: throw BadRequestException("Character not found")

                                    val localSession = GameSession(this, token, account, character)
                                    session = localSession

                                    trx.dsl().update(ACCOUNT)
                                        .set(ACCOUNT.WS_TOKEN, DSL.inline(null, ACCOUNT.WS_TOKEN))
                                        .where(ACCOUNT.ID.eq(account.id))
                                        .execute()

                                    // кикнуть таких же персонажей этого юзера
                                    // (можно заходить в игру своими разными персонажами одновременно)
                                    gameSessions.forEach { s ->
                                        if (s.character.id == character.id) {
                                            runBlocking {
                                                s.kick()
                                            }
                                        }
                                    }
                                    gameSessions += localSession
                                }
                            }
                            session!!.connected(request)
                        } else if (session != null) {
                            try {
                                session!!.process(request)
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

                    else -> {
                        logger.warn("unknown WS frame $frame")
                    }
                }
            }
        } finally {
            logger.debug("ws disconnected")
            if (session != null) {
                session?.disconnected()
                gameSessions -= session!!
            }
        }
    }
}