package com.origin.entity

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
    val email: Column<String?> = varchar("email", 64).nullable()
    val onlineTime: Column<Long> = long("onlineTime")
    val ssid: Column<String?> = char("ssid", 32).nullable()

}

//@Entity
//@Table(name = "accounts", indexes = [Index(name = "login_uniq", columnList = "login", unique = true)])
class Account(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Account>(Accounts)

    //    @Column(name = "login", columnDefinition = "VARCHAR(64) NOT NULL", nullable = false)
    var login by Accounts.login

    //    @Column(name = "password", columnDefinition = "VARCHAR(64) NOT NULL", nullable = false)
    var password by Accounts.password

    //    @Column(name = "email", columnDefinition = "VARCHAR(64) NULL", nullable = false)
    var email by Accounts.email

    //    @Column(name = "createTime", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    val createTime: Timestamp? = null

    //    @Column(name = "onlineTime", columnDefinition = "BIGINT UNSIGNED NOT NULL DEFAULT 0")
    var onlineTime by Accounts.onlineTime

    //    @Column(name = "ssid", columnDefinition = "CHAR(32) NULL DEFAULT NULL")
    var ssid by Accounts.ssid

    fun generateSessionId() {
        ssid = Utils.generatString(32)
    }

    fun appendOnlineTime(v: Int) {
        onlineTime += v.toLong()
    }
}