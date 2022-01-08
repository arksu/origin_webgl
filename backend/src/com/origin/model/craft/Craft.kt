package com.origin.model.craft

import com.origin.model.inventory.ItemType as it
import com.origin.model.skills.Skill
import com.origin.model.inventory.ItemWithCount as item

enum class Craft(
    val produce: List<item>,
    val required: List<item>,
    val skills: Set<Skill>? = null,
) {
    STONE_AXE(
        listOf(item(it.STONE_AXE)),
        listOf(item(it.STONE), item(it.BRANCH, 2))
    ),
    BUCKET(
        listOf(item(it.BUCKET)),
        listOf(item(it.BOARD, 2)),
        setOf(Skill.CARPENTRY)
    )
}
