package com.origin

import com.origin.config.DatabaseConfig
import com.origin.config.ServerConfig
import com.origin.model.item.ItemFactory
import com.origin.model.`object`.ObjectsFactory
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

@ObsoleteCoroutinesApi
object ServerLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty("org.jooq.no-logo", "true")
        System.setProperty("org.jooq.no-tips", "true")

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

        ObjectsFactory.init()
        ItemFactory.init()

//        FileWatcher.start()
//        EventBus.init()
        TimeController.start()
        GameWebServer.start()
    }
}
