package com.origin

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
        ServerConfig.load()
        DatabaseFactory.init()
        GameServer.start()
    }
}