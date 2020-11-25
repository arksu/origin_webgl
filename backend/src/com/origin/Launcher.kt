package com.origin

import com.origin.ServerConfig.loadConfig
import com.origin.net.GameServer
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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


        val server = embeddedServer(Netty, 8010) {
            routing {
                get("/") {
                    call.respondText("Hello, world!", ContentType.Text.Html)
                }
            }
        }
        server.start(wait = false)

        val gameServer = GameServer(InetSocketAddress("0.0.0.0", 7070), Runtime.getRuntime().availableProcessors())
        gameServer.start()
    }
}