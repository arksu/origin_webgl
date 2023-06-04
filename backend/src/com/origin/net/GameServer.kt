package com.origin.net

import com.origin.AccountCache
import com.origin.ServerConfig
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@ObsoleteCoroutinesApi
val logger: Logger = LoggerFactory.getLogger(GameServer::class.java)

@ObsoleteCoroutinesApi
object GameServer {
    val accountCache = AccountCache()

    val SSID_HEADER = HttpHeaders.Authorization

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
                    call.respondText("Hello from world of <Origin>!", ContentType.Text.Plain)
                }
                api()
                websockets()
            }
        }
        server.start(wait = true)
    }
}
