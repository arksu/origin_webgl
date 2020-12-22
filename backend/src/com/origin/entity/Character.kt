package com.origin.entity

import com.origin.timestamp
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

/**
 * игровой персонаж игрока
 */
object Characters : EntityPositions("characters") {
    /**
     * ид аккаунта к которому привязан персонаж
     */
    val account = reference("account", Accounts)

    /**
     * имя персонажа (выводим на головой в игровом клиенте)
     */
    val name = varchar("name", 16)

    /**
     * когда был создан персонаж
     */
    val createTime = timestamp("createTime", true).nullable()
    val onlineTime = long("onlineTime").default(0)
}

class Character(id: EntityID<Int>) : EntityPosition(id) {
    companion object : IntEntityClass<Character>(Characters)

    var account by Account referencedOn Characters.account
    var name by Characters.name

    var onlineTime by Characters.onlineTime

    fun appendOnlineTime(v: Int) {
        onlineTime += v.toLong()
    }
}