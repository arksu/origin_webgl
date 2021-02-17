package com.origin.model.items

import com.origin.entity.InventoryItemEntity
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
object ItemsFactory {
    fun byEntity(inventory: Inventory, entity: InventoryItemEntity): InventoryItem {
        return when (entity.type) {
            1 -> InventoryItem(inventory, entity)
            else -> InventoryItem(inventory, entity)
        }
    }
}