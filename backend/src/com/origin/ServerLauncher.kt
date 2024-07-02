package com.origin

import com.origin.config.DatabaseConfig
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.flywaydb.core.Flyway
import java.util.*

@ObsoleteCoroutinesApi
object ServerLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "-mapgen" -> {
//                    MapGenerator.run()
                }

                "-mapimport" -> {
//                    MapImporter.run()
                }

                "-run" -> {
                    run()
                }
            }
        } else {
            run()
        }
    }

    private fun run() {
        Locale.setDefault(Locale.ROOT)
        ServerConfig.load()
        DatabaseConfig.flywayMigrate()

//        FileWatcher.start()
        EventBus.init()
//        DatabaseFactory.init()
//        TimeController.start()
        GameWebServer.start()
    }
}
