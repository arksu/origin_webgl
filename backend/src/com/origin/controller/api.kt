package com.origin.controller

import com.origin.GameServer
import com.origin.config.DatabaseConfig
import com.origin.error.AuthorizationException
import com.origin.jooq.tables.records.AccountRecord
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * REST Api для авторизации и операций с персонажами (до игровое состояние)
 * основная игра идет в Websockets канале
 */
@ObsoleteCoroutinesApi
fun Route.api() {
    route("/api") {
        auth(DatabaseConfig.dsl)
        characters(DatabaseConfig.dsl)
    }
}

fun ApplicationCall.getAccountSsid(): String {
    return this.request.headers[HttpHeaders.Authorization] ?: throw AuthorizationException()
}

inline fun PipelineContext<Unit, ApplicationCall>.withAccount(block: (account: AccountRecord) -> Unit) {
    val accountSsid = call.getAccountSsid()
    val account = GameServer.accountCache.get(accountSsid) ?: throw AuthorizationException()
    block(account)
}