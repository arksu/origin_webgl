package com.origin.net

import com.origin.AccountCache
import com.origin.net.api.UserNotFound
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.*
import io.ktor.websocket.*
import org.slf4j.LoggerFactory

@KtorExperimentalAPI
object GameServer {
    fun start() {
        val server = embeddedServer(CIO, port = 8020) {
            install(StatusPages) {
                exception<UserNotFound> {
                    call.respond(HttpStatusCode.Forbidden, "User not found")
                }
                exception<Throwable> { cause ->
                    call.respond(HttpStatusCode.InternalServerError, cause.message!!)
                }
            }
            install(CORS) {
                cors()
            }
            install(WebSockets)
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }

            routing {
                get("/") {
                    call.respondText("Hello, world!", ContentType.Text.Plain)
                }
                api()
            }
        }
        server.start(wait = true)
    }

    private val _log = LoggerFactory.getLogger(GameServer::class.java.name)
    private val accountCache = AccountCache()

/*

    /**
     * получить список персонажей
     */
    fun getCharacters(account: Account, data: Map<String, Any>?): Any {
        return Database.em()
            .findAll(Character::class.java, "SELECT * FROM characters WHERE accountId=? limit 5", account.id)
    }

    /**
     * создать нового персонажа
     */
    fun createCharacter(account: Account, data: Map<String, Any>): Any {
        val name = data["name"] as String?
        val character = Character()
        character.name = name
        character.accountId = account.id
        character.persist()
        return Database.em()
            .findAll(Character::class.java, "SELECT * FROM characters WHERE accountId=? limit 5", account.id)
    }

    /**
     * удалить персонажа
     */
    fun deleteCharacter(account: Account, data: Map<String, Any>): Any {
        val character = Character()
        character.id = Math.toIntExact((data["id"] as Long?)!!)
        Database.em().remove(character)
        return Database.em()
            .findAll(Character::class.java, "SELECT * FROM characters WHERE accountId=? limit 5", account.id)
    }

    /**
     * выбрать игровой персонаж
     */
    @Throws(GameException::class)
    fun selectCharacter(session: GameSession?, data: Map<String, Any>): Any {
        val character = Database.em().findById(
            Character::class.java, Math.toIntExact(
                (data["id"] as Long?)!!
            )
        ) ?: throw GameException("no such player")
        val player = Player(character, session!!)
        if (!World.instance.spawnPlayer(player)) {
            throw GameException("player could not be spawned")
        }
        return character
    }



    @Throws(InterruptedException::class)
    private fun loginUser(session: GameSession, account: Account): Any {
        session.account = account
        if (!accountCache.addWithAuth(account)) {
            throw GameException("ssid collision, please try again")
        }
        val response = LoginResponse()
        response.ssid = account.ssid
        return response
    }

    /**
     * обработка всех websocket запросов
     */
    @Throws(Exception::class)
    override fun process(session: GameSession, target: String?, data: Map<String, Any>?): Any? {
        // если к сессии еще не привязан юзер
        if (session.account == null) {

            when (target) {
                "login" -> return login(session, data!!)
                "register" -> return registerNewAccount(session, data!!)
            }
        } else {
            when (target) {
                "getCharacters" -> return getCharacters(session.account!!, data)
                "createCharacter" -> return createCharacter(session.account!!, data!!)
                "selectCharacter" -> return selectCharacter(session, data!!)
                "deleteCharacter" -> return deleteCharacter(session.account!!, data!!)
            }
        }
        _log.warn("unknown command: {}", target)
        return null
    }

 */
}