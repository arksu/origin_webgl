package com.origin.net

import com.origin.AccountCache
import com.origin.Database
import com.origin.entity.Account
import com.origin.entity.Character
import com.origin.model.Player
import com.origin.model.World
import com.origin.net.model.GameSession
import com.origin.net.model.LoginResponse
import com.origin.scrypt.SCryptUtil
import com.origin.utils.GameException
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.sql.SQLException

class GameServer(address: InetSocketAddress?, decoderCount: Int) : WSServer(address, decoderCount) {
    companion object {
        private val _log = LoggerFactory.getLogger(GameServer::class.java.name)
        private val accountCache = AccountCache()
    }

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

    /**
     * регистрация нового аккаунта
     */
    @Throws(InterruptedException::class, GameException::class)
    fun registerNewAccount(session: GameSession, data: Map<String, Any>): Any {
        val account = Account()
        account.login = data["login"] as String?
        account.password = data["password"] as String?
        account.email = data["email"] as String?
        return try {
            account.persist()
            loginUser(session, account)
        } catch (e: RuntimeException) {
            if (e.cause is SQLException && "23000" == (e.cause as SQLException?)!!.sqlState) {
                throw GameException("username busy")
            } else {
                throw GameException("register failed")
            }
        } catch (e: Throwable) {
            throw GameException("register failed")
        }
    }

    /**
     * вход в систему
     */
    @Throws(InterruptedException::class, GameException::class)
    fun login(session: GameSession, data: Map<String, Any>): Any {
        val login = data["login"] as String?
        val password = data["password"] as String?
        val account = Database.em().findOne(Account::class.java, "login", login)
        return if (account != null) {
            if (SCryptUtil.check(account.password, password)) {
                _log.debug("user auth: " + account.login)
                loginUser(session, account)
            } else {
                throw GameException("wrong password")
            }
        } else {
            throw GameException("user not found")
        }
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
}