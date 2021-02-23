package com.origin.model.inventory

import com.origin.entity.InventoryItemEntity
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
object ItemsFactory {
    fun byEntity(inventory: Inventory, entity: InventoryItemEntity): InventoryItem {
        return InventoryItem(entity, inventory)
    }

    fun getIcon(entity: InventoryItemEntity): String {
        return when (entity.type) {
            1 -> "/items/stone.png"
            2 -> "/items/apple.png"
            3 -> "/items/bone.png"
            4 -> "/items/rabbit.png"
            5 -> "/items/board.png"
            else -> "/items/board.png"
        }
    }
}