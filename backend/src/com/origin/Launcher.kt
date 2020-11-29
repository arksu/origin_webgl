package com.origin

import com.origin.ServerConfig.loadConfig
import com.origin.net.api
import com.origin.net.cors
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
import java.util.*

data class UserLogin(val login: String, val hash: String)
data class UserSignup(val login: String, val email: String?, val password: String)
data class LoginResponse(val ssid: String?, val error: String? = null)

val logger = LoggerFactory.getLogger(Launcher::class.java)

object Launcher {

    @KtorExperimentalAPI
    @JvmStatic
    fun main(args: Array<String>) {
        Locale.setDefault(Locale.ROOT)
        loadConfig()
//        DatabaseDeprecated.start()
        logger.debug("start game server...")

        DatabaseFactory.init()

        val server = embeddedServer(CIO, port = 8010) {
            install(CORS) {
                cors()
            }
            install(WebSockets)
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
            }
        }
        server.start(wait = true)
    }
}