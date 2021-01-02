package com.origin.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

/**
 * предмет в инвентаре
 */
object InventoryItems : LongIdTable("inventory") {
    /**
     * ид инвентаря (родителя, вещи в которой находится этот предмет
     */
    val inventoryId = integer("inventoryId")

    /**
     * тип предмета
     */
    val type = integer("type")

    /**
     * положение внутри инвентаря
     */
    val x = integer("x")
    val y = integer("y")

    /**
     * качество вещи
     */
    val quality = integer("quality")

    /**
     * количество в стаке
     */
    val count = integer("count").default(1)

    /**
     * тик (если вещь может имзенятся с течением времени
     */
    val tick = integer("tick").default(0)
}

class InventoryItem(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<InventoryItem>(InventoryItems)

    var inventoryId by InventoryItems.inventoryId
    var type by InventoryItems.type
    var x by InventoryItems.x
    var y by InventoryItems.y
    var quality by InventoryItems.quality
    var count by InventoryItems.count
    var tick by InventoryItems.tick
}