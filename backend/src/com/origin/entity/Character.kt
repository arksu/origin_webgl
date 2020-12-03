package com.origin.entity

import com.origin.timestamp
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * игровой персонаж игрока
 */
object Characters : IntIdTable("characters") {
    /**
     * ид аккаунта к которому привязан персонаж
     */
    val account = reference("account", Accounts)

    /**
     * имя персонажа (выводим на головой в игровом клиенте)
     */
    val name = varchar("name", 16)

    /**
     * на каком континенте находится игрок, либо ид дома (инстанса, локации)
     */
    val region = integer("region")

    /**
     * координаты в игровых еденицах внутри континента (из этого расчитываем супергрид и грид)
     */
    val x = integer("x")
    val y = integer("y")

    /**
     * уровень (слой) глубины где находится игрок
     */
    val level = integer("level")

    /**
     * когда был создан персонаж
     */
    val createTime = timestamp("createTime", true).nullable()
    val onlineTime = long("onlineTime").default(0)
}

class Character(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Character>(Characters)

    var account by Account referencedOn Characters.account
    var name by Characters.name
    var region by Characters.region
    var x by Characters.x
    var y by Characters.y
    var level by Characters.level
    var onlineTime by Characters.onlineTime

    fun appendOnlineTime(v: Int) {
        onlineTime += v.toLong()
    }
}