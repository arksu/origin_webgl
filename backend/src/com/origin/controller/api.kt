package com.origin.controller

import com.origin.config.DatabaseConfig
import io.ktor.server.routing.*
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
