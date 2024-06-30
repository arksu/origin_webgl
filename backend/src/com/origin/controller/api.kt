package com.origin.controller

import com.origin.GameWebServer
import com.origin.config.DatabaseConfig
import com.origin.error.AuthorizationException
import com.origin.jooq.tables.records.AccountRecord
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

/**
 * REST Api для авторизации и операций с персонажами (до игровое состояние)
 * основная игра идет в Websockets канале
 */
fun Route.api() {
    route("/api") {
        auth(DatabaseConfig.dsl)
        characters(DatabaseConfig.dsl)
        websockets(DatabaseConfig.dsl)
    }
}

fun ApplicationCall.getAccountSsid(): String {
    val bearerPrefix = "Bearer "

    val authorizationHeader = this.request.headers[HttpHeaders.Authorization] ?: throw AuthorizationException()
    return if (authorizationHeader.startsWith(bearerPrefix, ignoreCase = true)) {
        authorizationHeader.substring(bearerPrefix.length).trim()
    } else {
        authorizationHeader.trim()
    }
}

inline fun PipelineContext<Unit, ApplicationCall>.withAccount(block: (account: AccountRecord) -> Unit) {
    val accountSsid = call.getAccountSsid()
    val account = GameWebServer.accountCache.get(accountSsid) ?: throw AuthorizationException()
    block(account)
}