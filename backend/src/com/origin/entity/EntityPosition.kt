package com.origin.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * позиция объектов в игровом мире
 * общие поля для всех игровых объектов
 */
abstract class EntityPositions(name: String = "", columnName: String = "id") : LongIdTable(name, columnName) {
    /**
     * на каком континенте находится объект, либо ид дома (инстанса, локации)
     */
    val region = integer("region")

    /**
     * координаты в игровых еденицах внутри континента (из этого расчитываем супергрид и грид)
     */
    val x = integer("x")
    val y = integer("y")

    /**
     * уровень (слой) глубины где находится объект
     */
    val level = integer("level")

    /**
     * угол поворота
     */
    val heading = short("heading")
}

abstract class EntityPosition(id: EntityID<Long>) : LongEntity(id) {
    var region by Characters.region
    var x by Characters.x
    var y by Characters.y
    var level by Characters.level
    var heading by Characters.heading
}