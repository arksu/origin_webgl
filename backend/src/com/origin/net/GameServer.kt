package com.origin.net

import com.origin.AccountCache
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.*
import io.ktor.websocket.*
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger(GameServer::class.java)

object GameServer {
    val accountCache = AccountCache()

    val SSID_HEADER = HttpHeaders.Authorization

    @KtorExperimentalAPI
    fun start() {
        val server = embeddedServer(CIO, port = 8020) {
            install(StatusPages) {
                statusPages()
            }
            install(CORS) {
                cors()
            }
            install(WebSockets) {
                websockets()
            }
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }

            routing {
                get("/") {
                    call.respondText("Hello, world!", ContentType.Text.Plain)
                }
                api()
                websockets()
            }
        }
        server.start(wait = true)
    }
}