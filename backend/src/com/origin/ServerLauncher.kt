package com.origin

import kotlinx.coroutines.ObsoleteCoroutinesApi
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
//        FileWatcher.start()
        EventBus.init()
//        DatabaseFactory.init()
//        TimeController.start()
        GameServer.start()
    }
}
