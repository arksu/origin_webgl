package com.origin

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.origin.controller.api
import com.origin.error.*
import com.origin.util.MapDeserializerDoubleAsIntFix
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

object GameWebServer {
    val logger: Logger = LoggerFactory.getLogger(GameWebServer::class.java)

    val accountCache = AccountCache()

    val gsonDeserializer: Gson = GsonBuilder()
        .registerTypeAdapter(
            object : TypeToken<Map<String, Any>>() {}.type, MapDeserializerDoubleAsIntFix()
        )
        .create()

    fun start() {
        logger.info("start game server [${ServerConfig.SERVER_PORT}]...")

        val server = embeddedServer(CIO, port = ServerConfig.SERVER_PORT) {
            install(DefaultHeaders)
            install(CallLogging)
            install(StatusPages) {
                statusPages()
            }
            install(CORS) {
                cors()
            }
            install(WebSockets) {
                // websockets config options
                pingPeriod = Duration.ofSeconds(15)
            }
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }

            routing {
                get("/") {
                    call.respondText("Hello from world of <Origin>!", ContentType.Text.Plain)
                }
                api()
            }
        }
        server.start(wait = true)
    }
}

fun CORSConfig.cors() {
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Put)
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.Authorization)
    anyHost()
}

fun StatusPagesConfig.statusPages() {
    exception<UserNotFoundException> { call, _ ->
        call.respond(HttpStatusCode.Forbidden, "User not found")
    }
    exception<AuthorizationException> { call, e ->
        call.respond(HttpStatusCode.Unauthorized, e.message ?: "Unauthorized")
    }
    exception<WrongPasswordException> { call, _ ->
        call.respond(HttpStatusCode.Forbidden, "Wrong password")
    }
    exception<UserAlreadyExistsException> { call, _ ->
        call.respond(HttpStatusCode.Forbidden, "User exists")
    }
    exception<BadRequestException> { call, e ->
        call.respond(HttpStatusCode.BadRequest, e.message!!)
    }
    exception<Throwable> { call, e ->
        GameWebServer.logger.error("error ${e.javaClass.simpleName} - ${e.message} ", e)
        call.respond(HttpStatusCode.InternalServerError, e.message ?: e.javaClass.simpleName)
    }
}