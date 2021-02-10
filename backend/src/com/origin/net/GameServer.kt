package com.origin.net

import com.origin.AccountCache
import com.origin.ServerConfig
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
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@ObsoleteCoroutinesApi
val logger: Logger = LoggerFactory.getLogger(GameServer::class.java)

@ObsoleteCoroutinesApi
object GameServer {
    val accountCache = AccountCache()

    val SSID_HEADER = HttpHeaders.Authorization

    @KtorExperimentalAPI
    fun start() {
        logger.info("start game server...")

        val server = embeddedServer(CIO, port = ServerConfig.PORT) {
            install(DefaultHeaders)
            install(CallLogging)
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
                    call.respondText("Hello, origin-world!", ContentType.Text.Plain)
                }
                api()
                websockets()
            }
        }
        server.start(wait = true)
    }
}