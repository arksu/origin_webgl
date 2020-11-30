package com.origin

import com.origin.ServerConfig.loadConfig
import com.origin.net.GameServer
import io.ktor.util.*
import org.slf4j.LoggerFactory
import java.util.*

data class UserLogin(val login: String, val hash: String)
data class UserSignup(val login: String, val email: String?, val password: String)
data class LoginResponse(val ssid: String?, val error: String? = null)

val logger = LoggerFactory.getLogger(Launcher::class.java)

object Launcher {

    @KtorExperimentalAPI
    @JvmStatic
    fun main(args: Array<String>) {
        Locale.setDefault(Locale.ROOT)
        loadConfig()
//        DatabaseDeprecated.start()
        logger.debug("start game server...")

        DatabaseFactory.init()

        GameServer.start()
    }
}