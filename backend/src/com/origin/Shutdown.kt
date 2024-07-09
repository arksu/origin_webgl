package com.origin

import kotlinx.coroutines.runBlocking

/**
 * Поток для полной остановки сервера и корректного сохранения всех данных
 */
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
