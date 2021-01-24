package com.origin

import com.origin.utils.TILE_SIZE
import com.typesafe.config.ConfigFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object ServerConfig {
    val logger: Logger = LoggerFactory.getLogger(ServerConfig::class.java)

    private const val WORK_DIR = "./"

    const val PROTO_VERSION = "0.0.1"

    @JvmField
    var PORT = 0

    @JvmField
    var DB_HOST: String? = null

    @JvmField
    var DB_USER: String? = null

    @JvmField
    var DB_PASSWORD: String? = null

    @JvmField
    var DB_NAME: String? = null

    /**
     * расстояние через которое будет обновлятся позиция в базе данных при передвижении
     */
    var UPDATE_DB_DISTANCE = TILE_SIZE * 5

    /**
     * дистанция которую нужно пройти чтобы произошел апдейт видимых объектов
     */
    var VISIBLE_UPDATE_DISTANCE = TILE_SIZE * 1

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
        val conf = ConfigFactory
            .parseFile(configFile)
            .withFallback(ConfigFactory.load("server.defaults.conf"))

        DB_HOST = conf.getString("origin.db.host")
        DB_USER = conf.getString("origin.db.user")
        DB_PASSWORD = conf.getString("origin.db.password")
        DB_NAME = conf.getString("origin.db.name")
        PORT = conf.getInt("origin.net.port")
        IS_DEV = conf.getBoolean("origin.dev.mode")
        ASSETS_DIR = conf.getString("origin.dev.assets_dir")
    }
}