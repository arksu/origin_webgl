package com.origin.model.inventory

enum class ItemType(val id: Int, val icon: String, val width: Int = 1, val height: Int = 1) {
    STONE(1, "/items/stone.png"),
    APPLE(2, "/items/apple.png"),
    BRANCH(3, "/items/branch.png"),
    RABBIT(4, "/items/rabbit.png", 2, 2),
    BOARD(5, "/items/board.png", 1, 4),
    BARK(6, "/items/bark.png"),
    STONE_AXE(7, "/items/stone_axe.png"),
    BUCKET(8, "/items/bucket.png");

    companion object {
        private val map = values().associateBy(ItemType::id)
        fun fromId(id: Int) = map[id] ?: throw IllegalArgumentException("item type not found $id")
    }
}

data class ItemWithCount(
    val item: ItemType,
    val count: Int = 1
)
