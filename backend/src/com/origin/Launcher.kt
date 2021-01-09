package com.origin

import com.origin.net.GameServer
import io.ktor.util.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.LoggerFactory
import java.util.*

val logger = LoggerFactory.getLogger(Launcher::class.java)

@ObsoleteCoroutinesApi
object Launcher {

    @KtorExperimentalAPI
    @JvmStatic
    fun main(args: Array<String>) {
        Locale.setDefault(Locale.ROOT)
        ServerConfig.load()
        EventBus.init()
        DatabaseFactory.init()
        TimeController.instance.start()
        GameServer.start()
    }
}