package com.origin

import com.origin.database.DatabaseFactory
import com.origin.net.GameServer
import com.origin.utils.MapGenerator
import com.origin.utils.MapImporter
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@ObsoleteCoroutinesApi
object ServerLauncher {
    val logger: Logger = LoggerFactory.getLogger(ServerLauncher::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "-mapgen" -> {
                    MapGenerator.run()
                }
                "-mapimport" -> {
                    MapImporter.run()
                }
                "-run" -> {
                    run()
                }
            }
        } else {
            run()
        }
    }

    fun run() {
        Locale.setDefault(Locale.ROOT)
        ServerConfig.load()
        FileWatcher.start()
        EventBus.init()
        DatabaseFactory.init()
        TimeController.start()
        GameServer.start()
    }
}
