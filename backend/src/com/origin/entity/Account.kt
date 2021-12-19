package com.origin.entity

import com.origin.database.timestamp
import com.origin.utils.Utils
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * аккаунт пользователя к которому может прикрепляться несколько персонажей
 */
object Accounts : IntIdTable("accounts") {
    val login = varchar("login", 64).uniqueIndex()
    val password = varchar("password", 64)
    val email = varchar("email", 64).nullable()

    /**
     * id сессии для доступа к REST Api
     */
    val ssid = char("ssid", 32).nullable().index()

    /**
     * одноразовый токен для коннекта к WS игровой части сервера
     */
    val wsToken = char("ws_token", 32).nullable().index()

    /**
     * ид выбранного персонажа на этапе логина
     */
    val selectedCharacter = long("selectedCharacter").nullable()

    /**
     * дата создания
     */
    val created = timestamp("createTime", true).nullable()

    /**
     * общее время в игре проведенное любыми персонажами аккаунта
     */
    val onlineTime = long("onlineTime").default(0)

    /**
     * время последнего логина
     */
//    val lastLogged = timestamp("lastLogged").default(Timestamp(1000))
    val deleted = bool("deleted").default(false)
}

class Account(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Account>(Accounts)

    var login by Accounts.login
    var password by Accounts.password
    var email by Accounts.email
    var onlineTime by Accounts.onlineTime
//    var lastLogged by Accounts.lastLogged
    var ssid by Accounts.ssid
    var wsToken by Accounts.wsToken
    var selectedCharacter by Accounts.selectedCharacter

    fun generateSessionId() {
        ssid = Utils.generateString(32)
    }

    fun generateWsToken() {
        wsToken = Utils.generateString(32)
    }

    fun appendOnlineTime(v: Int) {
        onlineTime += v.toLong()
    }
}
