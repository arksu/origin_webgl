package com.origin.entity

import com.origin.database.timestamp
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
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

    val SHP = double("SHP")
    val HHP = double("HHP")
    val stamina = double("stamina")
    val energy = double("energy")
    val hunger = double("hunger")

    /**
     * когда был создан персонаж
     */
    @Suppress("unused")
    val createTime = timestamp("createTime", true).nullable()

    /**
     * время последнего логина
     */
//    val lastLogged = timestamp("lastLogged").default(Timestamp(1000))

    /**
     * флаг уадаления
     */
    val deleted = bool("deleted").default(false)

    /**
     * сколько времени онлайн провел
     */
    val onlineTime = long("onlineTime").default(0)
}

class Character(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Character>(Characters)

    var region by Characters.region
    var x by Characters.x
    var y by Characters.y
    var level by Characters.level
    var heading by Characters.heading

    var account by Account referencedOn Characters.account
    var name by Characters.name
    var SHP by Characters.SHP
    var HHP by Characters.HHP
    var stamina by Characters.stamina
    var energy by Characters.energy
    var hunger by Characters.hunger

    var deleted by Characters.deleted

    var onlineTime by Characters.onlineTime
//    var lastLogged by Characters.lastLogged
}
