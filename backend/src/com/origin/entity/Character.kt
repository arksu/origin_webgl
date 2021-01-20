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

    /**
     * когда был создан персонаж
     */
    @Suppress("unused")
    val createTime = timestamp("createTime", true).nullable()

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

    private var onlineTime by Characters.onlineTime

    fun appendOnlineTime(v: Int) {
        onlineTime += v.toLong()
    }
}