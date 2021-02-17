package com.origin.model.items

import com.origin.entity.InventoryItemEntity
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.transactions.transaction

@ObsoleteCoroutinesApi
class InventoryItem(val inventory: Inventory, private val entity: InventoryItemEntity) {

    val x: Int
        get() {
            return entity.x
        }

    val y: Int
        get() {
            return entity.y
        }

    val width = 1

    val height = 1

    fun delete() {
        transaction {
            entity.deleted = true
        }
    }

    fun collide(x: Int, y: Int, w: Int, h: Int): Boolean {
        val tr = this.x + width - 1
        val tb = this.y + height - 1
        val r = x + w - 1
        val b = y + h - 1

        return (this.x in x..r || tr in x..r || x in this.x..tr || r in this.x..tr)
                && (this.y in y..b || tb in y..b || y in this.y..tb || b in this.y..tb)
    }
}