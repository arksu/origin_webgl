package com.origin.model.inventory

import com.origin.entity.InventoryItemEntity
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
object ItemsFactory {
    fun byEntity(inventory: Inventory, entity: InventoryItemEntity): InventoryItem {
        return when (entity.type) {
            1 -> InventoryItem(entity, inventory, "/items/rabbit.png")
            2 -> InventoryItem(entity, inventory, "/items/apple.png")
            else -> InventoryItem(entity, inventory, "/items/board.png")
        }
    }
}