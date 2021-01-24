package com.origin

import com.origin.database.DatabaseFactory
import com.origin.net.GameServer
import io.ktor.util.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


@ObsoleteCoroutinesApi
object Launcher {
    val logger: Logger = LoggerFactory.getLogger(Launcher::class.java)

    @KtorExperimentalAPI
    @JvmStatic
    fun main(args: Array<String>) {
        Locale.setDefault(Locale.ROOT)
        ServerConfig.load()
        FileWatcher.start()
        EventBus.init()
        DatabaseFactory.init()
        TimeController.start()
        GameServer.start()
    }
}