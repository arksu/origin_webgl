package com.origin

import com.origin.ServerConfig.loadConfig
import com.origin.net.GameServer
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.ServiceUnavailable
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.*

object Launcher {
    private val _log = LoggerFactory.getLogger(Launcher::class.java.name)

    @JvmStatic
    fun main(args: Array<String>) {
        Locale.setDefault(Locale.ROOT)
        loadConfig()
        Database.start()
        _log.debug("start game server...")


        val server = embeddedServer(Netty, port = 8010, configure = {
            runningLimit = 3
        }, module = {
            install(CORS) {
                method(HttpMethod.Post)
                header(HttpHeaders.ContentType)
                anyHost()
            }
            install(WebSockets)

            routing {
                get("/") {
                    call.respondText("Hello, world!", ContentType.Text.Html)
                }
                post("/login") {
                    println("login req")
                    call.response.status(ServiceUnavailable)
                    call.respondText { "ok" }
                }

                val wsConnections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())

                webSocket("/ws") {
                    wsConnections += this
                    println("onConnect")
                    try {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val text = frame.readText()

                                    if (text.equals("bye", ignoreCase = true)) {
                                        close(CloseReason(CloseReason.Codes.NORMAL, "said bye"))
                                    }
                                }
                            }
                        }
                    } finally {
                        wsConnections -= this
                    }
                }
            }
        })
        server.start(wait = false)

        val gameServer = GameServer(InetSocketAddress("0.0.0.0", 7070), Runtime.getRuntime().availableProcessors())
        gameServer.start()
    }
}