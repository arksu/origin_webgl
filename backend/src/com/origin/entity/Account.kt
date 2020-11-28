package com.origin.entity

import com.origin.utils.Utils
import java.sql.Timestamp

/**
 * аккаунт пользователя к которому может прикрепляться несколько персонажей
 */
//@Entity
//@Table(name = "accounts", indexes = [Index(name = "login_uniq", columnList = "login", unique = true)])
class Account {
//    @Id
//    @Column(name = "id", columnDefinition = "INT(11) UNSIGNED NOT NULL AUTO_INCREMENT")
//    @ColumnExtended(updateInsertId = true)
    val id = 0

//    @Column(name = "login", columnDefinition = "VARCHAR(64) NOT NULL", nullable = false)
    var login: String? = null

//    @Column(name = "password", columnDefinition = "VARCHAR(64) NOT NULL", nullable = false)
    var password: String? = null

//    @Column(name = "email", columnDefinition = "VARCHAR(64) NULL", nullable = false)
    var email: String? = null

//    @Column(name = "createTime", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    val createTime: Timestamp? = null

//    @Column(name = "onlineTime", columnDefinition = "BIGINT UNSIGNED NOT NULL DEFAULT 0")
    var onlineTime: Long = 0
        private set

//    @Column(name = "ssid", columnDefinition = "CHAR(32) NULL DEFAULT NULL")
    var ssid: String? = null

    fun generateSessionId() {
        ssid = Utils.generatString(32)
    }

    fun appendOnlineTime(v: Int) {
        onlineTime += v.toLong()
    }
}