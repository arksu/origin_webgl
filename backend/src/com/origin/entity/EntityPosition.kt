package com.origin.entity

import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * позиция объектов в игровом мире
 * общие поля для всех игровых объектов
 */
open class EntityPositions(name: String = "", columnName: String = "id") : LongIdTable(name, columnName) {
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
