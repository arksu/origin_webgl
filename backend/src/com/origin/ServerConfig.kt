package com.origin

import com.typesafe.config.ConfigFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object ServerConfig {
    private val logger: Logger = LoggerFactory.getLogger(ServerConfig::class.java)

    private const val WORK_DIR = "./"

    const val PROTO_VERSION = "0.0.2"

    @JvmField
    var SERVER_PORT = 0

    @JvmField
    var DATABASE_URL: String? = null

    @JvmField
    var DATABASE_USER: String? = null

    @JvmField
    var DATABASE_PASSWORD: String? = null

    /**
     * расстояние через которое будет обновлятся позиция в базе данных при передвижении
     */
    var UPDATE_DB_DISTANCE = TILE_SIZE * 10

    /**
     * дистанция которую нужно пройти чтобы произошел апдейт видимых объектов
     */
    var VISIBLE_UPDATE_DISTANCE = TILE_SIZE * 2

    /**
     * сервер запущен в дев режиме (слежение за каталогами с графикой и др фичи)
     */
    var IS_DEV = false

    lateinit var ASSETS_DIR: String

    @JvmStatic
    fun load() {
        logger.info("Load config...")

        // ищем конфиг в папке с конфигом
        var configFile = File(WORK_DIR + "config/server.conf")
        // если его там нет ищем в корне приложения
        if (!configFile.exists()) {
            configFile = File(WORK_DIR + "server.conf")
        }
        val config = ConfigFactory
            .parseFile(configFile)
            .withFallback(ConfigFactory.load("server.defaults.conf"))

        DATABASE_URL = config.getString("origin.database.url")
        DATABASE_USER = config.getString("origin.database.user")
        DATABASE_PASSWORD = config.getString("origin.database.password")

        SERVER_PORT = config.getInt("origin.net.port")

        IS_DEV = config.getBoolean("origin.dev.mode")
        ASSETS_DIR = config.getString("origin.dev.assets_dir")
    }
}
