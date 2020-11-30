package com.origin.entity

import com.origin.timestamp
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.sql.Timestamp

/**
 * игровой персонаж игрока
 */
object Characters : IntIdTable("characters") {
    /**
     * ид аккаунта к которому привязан персонаж
     */
    val accountId: Column<Int> = integer("accountId")

    /**
     * имя персонажа (выводим на головой в игровом клиенте)
     */
    val name: Column<String?> = varchar("name", 16).nullable()

    /**
     * на каком континенте находится игрок, либо ид дома (инстанса, локации)
     */
    val region: Column<Int> = integer("region")

    /**
     * координаты в игровых еденицах внутри континента (из этого расчитываем супергрид и грид)
     */
    val x: Column<Int> = integer("x")
    val y: Column<Int> = integer("y")

    /**
     * уровень (слой) глубины где находится игрок
     */
    val level: Column<Int> = integer("level")

    /**
     * когда был создан персонаж
     */
    val createTime: Column<Timestamp?> = timestamp("createTime", true).nullable()
    val onlineTime: Column<Long> = long("onlineTime").default(0)
}

class Character(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Character>(Characters)

    var accountId by Characters.accountId
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