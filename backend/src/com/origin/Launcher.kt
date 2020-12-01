package com.origin

import com.origin.ServerConfig.loadConfig
import com.origin.net.GameServer
import io.ktor.util.*
import org.slf4j.LoggerFactory
import java.util.*

val logger = LoggerFactory.getLogger(Launcher::class.java)

object Launcher {

    @KtorExperimentalAPI
    @JvmStatic
    fun main(args: Array<String>) {
        Locale.setDefault(Locale.ROOT)
        loadConfig()
        logger.debug("start game server...")

        DatabaseFactory.init()

        GameServer.start()
    }
}