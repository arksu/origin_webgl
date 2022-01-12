package com.origin.model.craft

import com.origin.model.skills.Skill
import com.origin.model.inventory.ItemType as it
import com.origin.model.inventory.ItemWithCount as item

class Craft(
    val name: String,
    val produce: List<item>,
    val required: List<item>,
    val skills: Set<Skill.Type>? = null,
)

val craftList = listOf(
    Craft(
        "STONE_AXE",
        listOf(item(it.STONE_AXE)),
        listOf(item(it.STONE), item(it.BRANCH, 2), item(it.BOARD))
    ),
    Craft(
        "IRON_AXE",
        listOf(item(it.STONE_AXE)),
        listOf(item(it.APPLE, 3), item(it.BRANCH, 2))
    ),
    Craft(
        "BUCKET",
        listOf(item(it.BUCKET)),
        listOf(item(it.BOARD, 2)),
        setOf(Skill.Type.CARPENTRY)
    ),
)
