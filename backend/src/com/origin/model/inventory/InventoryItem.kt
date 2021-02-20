package com.origin.model.inventory

import com.origin.entity.InventoryItemEntity
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * вещь в инвентаре / эквипе / руке и тд
 */
@ObsoleteCoroutinesApi
class InventoryItem(
    private val entity: InventoryItemEntity,
    private var inventory: Inventory,
    val icon: String,
) {

    val id: ObjectID
        get() {
            return entity.id.value
        }

    var x: Int = entity.x

    var y: Int = entity.y

    var q: Int = entity.quality

    // TODO
    val width = 1

    val height = 1

    fun putTo(inv: Inventory, x: Int, y: Int) {
        this.inventory = inv
        this.x = x
        this.y = y
        transaction {
            entity.inventoryId = inventory.inventoryId
            entity.x = x
            entity.y = y
        }
    }

    fun setXY(x: Int, y: Int) {
        this.x = x
        this.y = y
        transaction {
            entity.x = x
            entity.y = y
        }
    }

    fun delete() {
        transaction {
            entity.deleted = true
        }
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

        return (this.x in x..r || tr in x..r || x in this.x..tr || r in this.x..tr)
                && (this.y in y..b || tb in y..b || y in this.y..tb || b in this.y..tb)
    }
}