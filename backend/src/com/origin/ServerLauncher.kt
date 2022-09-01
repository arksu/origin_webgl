package com.origin

import com.origin.database.DatabaseFactory
import com.origin.net.GameServer
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@ObsoleteCoroutinesApi
object ServerLauncher {
    val logger: Logger = LoggerFactory.getLogger(ServerLauncher::class.java)

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
