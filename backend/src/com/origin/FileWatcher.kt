package com.origin

import com.origin.model.WorkerScope
import com.origin.model.World
import com.origin.net.model.FileAdded
import com.origin.net.model.FileChanged
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.CRC32

/**
 * проверяем все crc32 файлов ассетов и запоминаем
 * периодически сканируем директорию с ассетами
 * в случае каких либо изменений - шлем уведомление клиентам
 */
@ObsoleteCoroutinesApi
object FileWatcher {
    val logger: Logger = LoggerFactory.getLogger(FileWatcher::class.java)

    private val hash = HashMap<String, Long>()

    fun start() {
        if (!ServerConfig.IS_DEV || ServerConfig.ASSETS_DIR.isEmpty()) return

        logger.debug("start dev file watcher...")
        val len = ServerConfig.ASSETS_DIR.length
        try {
            Files.find(
                Paths.get(ServerConfig.ASSETS_DIR), Int.MAX_VALUE,
                { _: Path, fileAttr: BasicFileAttributes -> fileAttr.isRegularFile }
            )
                .forEach { x: Path ->
                    run {
                        val f = x.toString().substring(len)
                        hash[f] = getCRC(x)
                    }
                }

            // запустим поток который периодически проверяет хэши
            // и если изменилось отсылает на клиент уведомление
            val thread = Thread {
                while (true) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        break
                    }

                    Files.find(
                        Paths.get(ServerConfig.ASSETS_DIR), Int.MAX_VALUE,
                        { _: Path, fileAttr: BasicFileAttributes -> fileAttr.isRegularFile }
                    )
                        .forEach { x: Path ->
                            run {
                                val f = x.toString().substring(len)
                                if (!f.contains(".DS_Store")) {
                                    if (hash.containsKey(f)) {
                                        val crc = getCRC(x)
                                        if (hash[f] != crc) {
                                            logger.warn("changed file $f")
                                            sendChanged(f.replace("\\", "/"))
                                            hash[f] = crc
                                        }
                                    } else {
                                        logger.warn("new file $f")
                                        sendAdded(f.replace("\\", "/"))
                                        hash[f] = getCRC(x)
                                    }
                                }
                            }
                        }
                }
            }
            thread.start()
        } catch (e: Exception) {
            logger.error("error ${e.message}", e)
        }
    }

    private fun getCRC(x: Path): Long {
        val fin = Files.newInputStream(x)
        val crc = CRC32()
        val buffer = ByteArray(32 * 1024)
        var bytesRead: Int
        while (fin.read(buffer).also { bytesRead = it } != -1) {
            crc.update(buffer, 0, bytesRead)
        }
        fin.close()
        return crc.value
    }

    private fun sendChanged(f: String) {
        World.players.forEach { (_, player) ->
            WorkerScope.launch {
                player.session.send(FileChanged(f))
            }
        }
    }

    private fun sendAdded(f: String) {
        World.players.forEach { (_, player) ->
            WorkerScope.launch {
                player.session.send(FileAdded(f))
            }
        }
    }
}
