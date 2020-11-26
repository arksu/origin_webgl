package com.origin

import com.origin.ServerConfig.loadConfig
import com.origin.entity.Account
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*

class UserLogin(val login: String, val hash: String)
class LoginResponse(val ssid: String?, val error: String? = null)
class UserSignup(val login: String, val email: String?, val password: String)

object Launcher {
    private val _log = LoggerFactory.getLogger(Launcher::class.java.name)

    @JvmStatic
    fun main(args: Array<String>) {
        Locale.setDefault(Locale.ROOT)
        loadConfig()
        Database.start()
        _log.debug("start game server...")

        val server = embeddedServer(Netty, port = 8010) {
            install(CORS) {
                method(HttpMethod.Post)
                header(HttpHeaders.ContentType)
                anyHost()
            }
            install(WebSockets)
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }

            routing {
                get("/") {
                    call.respondText("Hello, world!", ContentType.Text.Html)
                }
                post("/login") {
                    val userLogin = call.receive<UserLogin>()
                    val account = Database.em().findOne(Account::class.java, "login", userLogin.login)

                    if (account == null) {
                        call.respond(LoginResponse(null, "account not found"))
                    } else {
                        // TODO auth , ssid
                        call.respond(LoginResponse("123"))
                    }
                }
                post("/signup") {
                    val userSignup = call.receive<UserSignup>()

                    val account = Account()
                    account.login = userSignup.login
                    account.password = userSignup.password
                    account.email = userSignup.email

                    try {
                        account.persist()
                        // TODO auth user
                        call.respond(LoginResponse("123"))
                    } catch (e: RuntimeException) {
                        _log.error("register failed RuntimeException ${e.message}", e)
                        if (e.cause is SQLException && "23000" == (e.cause as SQLException?)!!.sqlState) {
                            val vendorCode = (e.cause as SQLException?)!!.errorCode
                            if (vendorCode == 1062) {
                                call.respond(LoginResponse(null, "this username is busy"))
                            } else {
                                call.respond(LoginResponse(null, "register failed, vendor code $vendorCode"))
                            }
                        } else {
                            call.respond(LoginResponse(null, "register failed ${e.message}"))
                        }
                    } catch (e: Throwable) {
                        _log.error("register failed Throwable ${e.message}", e)
                        call.respond(LoginResponse(null, "register failed"))
                    }
                }

                val wsConnections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())

                webSocket("/ws") {
                    wsConnections += this
                    println("onConnect")
                    try {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val text = frame.readText()

                                    if (text.equals("bye", ignoreCase = true)) {
                                        close(CloseReason(CloseReason.Codes.NORMAL, "said bye"))
                                    }
                                }
                            }
                        }
                    } finally {
                        wsConnections -= this
                    }
                }
            }
        }
        server.start(wait = true)

//        val gameServer = GameServer(InetSocketAddress("0.0.0.0", 7070), Runtime.getRuntime().availableProcessors())
//        gameServer.start()
    }
}