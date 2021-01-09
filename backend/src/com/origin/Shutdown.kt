package com.origin

import com.origin.model.World
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking

/**
 * поток для полной остановки сервера и корректного сохранения всех данных
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
            World.disconnectAllCharacters()
        }
        TimeController.instance.shutdown()
    }

}