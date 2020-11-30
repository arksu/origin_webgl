package com.origin.entity

import com.origin.timestamp
import com.origin.utils.Utils
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.sql.Timestamp

/**
 * аккаунт пользователя к которому может прикрепляться несколько персонажей
 */
object Accounts : IntIdTable("accounts") {
    val login: Column<String> = varchar("login", 64).uniqueIndex()
    val password: Column<String> = varchar("password", 64)
    val email: Column<String?> = varchar("email", 64).nullable().uniqueIndex()
    val ssid: Column<String?> = char("ssid", 32).nullable()
    val createTime: Column<Timestamp?> = timestamp("createTime", true).nullable()
    val onlineTime: Column<Long> = long("onlineTime").default(0)
}

class Account(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Account>(Accounts)

    var login by Accounts.login

    var password by Accounts.password

    var email by Accounts.email

    var onlineTime by Accounts.onlineTime

    var ssid by Accounts.ssid

    fun generateSessionId() {
        ssid = Utils.generatString(32)
    }

    fun appendOnlineTime(v: Int) {
        onlineTime += v.toLong()
    }
}