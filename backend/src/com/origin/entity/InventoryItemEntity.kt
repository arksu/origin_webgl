package com.origin.entity

import com.origin.idfactory.IdFactory
import com.origin.model.inventory.ItemType
import kotlinx.coroutines.ObsoleteCoroutinesApi
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
    val inventoryId = long("inventoryId")

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
    val quality = short("quality").default(10)

    /**
     * количество в стаке
     */
    val count = integer("count").default(1)

    /**
     * тик (если вещь может имзенятся с течением времени
     */
    val tick = integer("tick").default(0)

    val deleted = bool("deleted").default(false)
}

@ObsoleteCoroutinesApi
class InventoryItemEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<InventoryItemEntity>(InventoryItems) {
        fun makeNew(t: ItemType, q: Short = 10): InventoryItemEntity {
            return InventoryItemEntity.new(IdFactory.getNext()) {
                type = t.id
                inventoryId = 0
                this.x = 0
                this.y = 0
                quality = q

                count = 1
                tick = 0
                deleted = false
            }
        }
    }

    var inventoryId by InventoryItems.inventoryId
    var type by InventoryItems.type
    var x by InventoryItems.x
    var y by InventoryItems.y
    var quality by InventoryItems.quality
    var count by InventoryItems.count
    var tick by InventoryItems.tick
    var deleted by InventoryItems.deleted
}