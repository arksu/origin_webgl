package com.origin.model.craft

import com.origin.model.inventory.ItemType
import com.origin.model.skills.Skill

enum class Craft(
    val produce: ItemType,
    val count: Int,
    val required: Set<ItemType>,
    val skills: Set<Skill>? = null,
) {
    StoneAxe(ItemType.STONE_AXE, 1, setOf(ItemType.STONE)),
    Bucket(ItemType.BUCKET, 1, setOf(ItemType.BOARD))
}
