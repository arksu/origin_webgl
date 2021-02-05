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
    val email = varchar("email", 64).nullable().uniqueIndex()
    val ssid = char("ssid", 32).nullable()
    val selectedCharacter = long("selectedCharacter").nullable()
    val created = timestamp("createTime", true).nullable()
    val onlineTime = long("onlineTime").default(0)
    val deleted = bool("deleted").default(false)
}

class Account(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Account>(Accounts)

    var login by Accounts.login
    var password by Accounts.password
    var email by Accounts.email
    var onlineTime by Accounts.onlineTime
    var ssid by Accounts.ssid
    var selectedCharacter by Accounts.selectedCharacter

    fun generateSessionId() {
        ssid = Utils.generateString(32)
    }

    fun appendOnlineTime(v: Int) {
        onlineTime += v.toLong()
    }
}