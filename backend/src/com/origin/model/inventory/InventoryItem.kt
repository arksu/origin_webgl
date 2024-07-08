package com.origin.model.inventory

import com.origin.ObjectID
import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.InventoryRecord
import com.origin.jooq.tables.references.INVENTORY

class InventoryItem(val record: InventoryRecord) {
    val id: ObjectID
        get() {
            return record.id
        }

    val type: ItemType = ItemType.fromId(record.type)

    val width get() = type.width
    val height get() = type.height
    val icon get() = type.icon

    val x: Int = record.x

    val y: Int = record.y

    val q: Short = record.quality

    /**
     * инвентарь кладет эту вещь в себя
     */
    fun inventoryPutTo(inv: Inventory, x: Int, y: Int) {
        record.inventoryId = inv.id
        record.x = x
        record.y = y

        DatabaseConfig.dsl
            .update(INVENTORY)
            .set(INVENTORY.INVENTORY_ID, record.inventoryId)
            .set(INVENTORY.X, record.x)
            .set(INVENTORY.Y, record.y)
            .where(INVENTORY.ID.eq(record.id))
            .execute()
    }

    fun setXY(x: Int, y: Int) {
        record.x = x
        record.y = y
        DatabaseConfig.dsl
            .update(INVENTORY)
            .set(INVENTORY.X, record.x)
            .set(INVENTORY.Y, record.y)
            .where(INVENTORY.ID.eq(record.id))
            .execute()
    }

    /**
     * удалить вещь из базы
     */
    fun delete() {
        record.deleted = 1
        DatabaseConfig.dsl
            .update(INVENTORY)
            .set(INVENTORY.DELETED, record.deleted)
            .where(INVENTORY.ID.eq(record.id))
            .execute()
    }

    /**
     * попадает ли вещь с указанной позицией и размерами в эту вещь? (есть пересечение?)
     * находится ли внутри этой вещи
     */
    fun collide(x: Int, y: Int, w: Int, h: Int): Boolean {
        val tr = this.x + type.width - 1
        val tb = this.y + type.height - 1
        val r = x + w - 1
        val b = y + h - 1

        return (this.x in x..r || tr in x..r || x in this.x..tr || r in this.x..tr) &&
                (this.y in y..b || tb in y..b || y in this.y..tb || b in this.y..tb)
    }
}