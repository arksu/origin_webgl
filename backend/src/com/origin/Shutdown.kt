package com.origin

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking

/**
 * Поток для полной остановки сервера и корректного сохранения всех данных
 */
@ObsoleteCoroutinesApi
class Shutdown : Thread() {

    companion object {
        fun start() {
            val worker = Shutdown()
            worker.start()
        }
    }

    override fun run() {
        runBlocking {
//            World.disconnectAllCharacters()
        }
//        TimeController.shutdown()
    }
}
