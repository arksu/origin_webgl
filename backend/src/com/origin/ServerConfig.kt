package com.origin

import com.origin.utils.TILE_SIZE
import com.typesafe.config.ConfigFactory
import java.io.File

object ServerConfig {
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
    }
}