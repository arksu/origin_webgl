package com.origin.model.inventory

import com.origin.entity.InventoryItemEntity
import kotlinx.coroutines.ObsoleteCoroutinesApi

enum class ItemType(val id: Int) {
    STONE(1),
    APPLE(2),
    BRANCH(3),
    RABBIT(4),
    BOARD(5),
    BARK(6),
}

@ObsoleteCoroutinesApi
object ItemsFactory {

    private val types = HashMap<Int, ItemTemplate>()

    init {
        types[ItemType.STONE.id] = ItemTemplate("/items/stone.png")
        types[ItemType.APPLE.id] = ItemTemplate("/items/apple.png")
        types[ItemType.BRANCH.id] = ItemTemplate("/items/bone.png")
        types[ItemType.RABBIT.id] = ItemTemplate("/items/rabbit.png", 2, 2)
        types[ItemType.BOARD.id] = ItemTemplate("/items/board.png", 1, 4)
        types[ItemType.BARK.id] = ItemTemplate("/items/bark.png")
    }

    fun getTemplate(entity: InventoryItemEntity): ItemTemplate {
        return types[entity.type] ?: return ItemTemplate("/items/unknown.png")
    }
}
