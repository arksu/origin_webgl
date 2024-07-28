package com.origin.model.item

import com.origin.ObjectID
import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.InventoryRecord
import com.origin.jooq.tables.references.INVENTORY
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.inventory.Inventory

abstract class Item(val record: InventoryRecord) {
    val id: ObjectID
        get() {
            return record.id
        }

    open val width = 1
    open val height = 1

    open fun getIcon(): String {
        return ItemFactory.getIcon(record.type)
    }

    val typeId: Int get() = record.type
    val x: Int get() = record.x
    val y: Int get() = record.y
    val quality: Short get() = record.quality
    val count = record.count

    var inventory: Inventory? = null

    /**
     * инвентарь кладет эту вещь в себя
     */
    fun onInventoryPut(inv: Inventory, x: Int, y: Int) {
        val oldInventoryId = record.inventoryId
        record.inventoryId = inv.id
        record.x = x
        record.y = y
        inventory = inv

        // если не был указан ид инвентаря - значит это спавн вещи. и надо ее вставить в таблицу,
        // иначе просто обновляем данные по вещи
        if (oldInventoryId == -1L) {
            DatabaseConfig.dsl
                .insertInto(INVENTORY)
                .set(record)
                .execute()
        } else {
            DatabaseConfig.dsl
                .update(INVENTORY)
                .set(INVENTORY.INVENTORY_ID, record.inventoryId)
                .set(INVENTORY.X, record.x)
                .set(INVENTORY.Y, record.y)
                .where(INVENTORY.ID.eq(record.id))
                .execute()
        }
    }

    fun onInventoryRemove() {
        inventory = null
    }

    open fun getContextMenu(player: Player): ContextMenu? {
        return null
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

    fun save() {
        DatabaseConfig.dsl
            .update(INVENTORY)
            .set(INVENTORY.TYPE, record.type)
            .set(INVENTORY.INVENTORY_ID, record.inventoryId)
            .set(INVENTORY.X, record.x)
            .set(INVENTORY.Y, record.y)
            .set(INVENTORY.QUALITY, record.quality)
            .set(INVENTORY.COUNT, record.count)
            .set(INVENTORY.LAST_TICK, record.lastTick)
            .set(INVENTORY.DATA, record.data)
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
        val tr = this.x + width - 1
        val tb = this.y + height - 1
        val r = x + w - 1
        val b = y + h - 1

        return (this.x in x..r || tr in x..r || x in this.x..tr || r in this.x..tr) &&
                (this.y in y..b || tb in y..b || y in this.y..tb || b in this.y..tb)
    }

    open suspend fun executeContextMenuItem(player: Player, selected: String) {
    }
}