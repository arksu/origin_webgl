package com.origin.model.items

import com.origin.entity.InventoryItemEntity
import com.origin.entity.InventoryItems
import com.origin.model.GameObject
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

@ObsoleteCoroutinesApi
class Inventory(parent: GameObject) {

    private val inventoryId: Long = parent.id

    val items = ConcurrentHashMap<ObjectID, InventoryItem>()

    fun getWidth(): Int {
        return 4
    }

    fun getHeight(): Int {
        return 4
    }

    init {
        transaction {
            val list = InventoryItemEntity.find { InventoryItems.inventoryId eq inventoryId }
            list.forEach {
                items[it.id.value] = ItemsFactory.byEntity(this@Inventory, it)
            }
        }
    }
}