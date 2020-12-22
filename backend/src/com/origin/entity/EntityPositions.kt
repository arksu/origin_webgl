package com.origin.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

open class EntityPositions(name: String = "", columnName: String = "id") : IntIdTable(name, columnName) {
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
     * угол поворота
     */
    val heading = integer("heading")
}

open class EntityPosition(id: EntityID<Int>) : IntEntity(id) {
    var region by Characters.region
    var x by Characters.x
    var y by Characters.y
    var level by Characters.level
    var heading by Characters.heading
}