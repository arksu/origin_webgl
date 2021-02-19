package com.origin.model.inventory

import com.origin.entity.InventoryItemEntity
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
object ItemsFactory {
    fun byEntity(inventory: Inventory, entity: InventoryItemEntity): InventoryItem {
        return when (entity.type) {
            1 -> InventoryItem(inventory, entity, "/items/rabbit.png")
            2 -> InventoryItem(inventory, entity, "/items/apple.png")
            else -> InventoryItem(inventory, entity, "/items/board.png")
        }
    }
}