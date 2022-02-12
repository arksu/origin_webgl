package com.origin.model.craft

import com.origin.model.skills.Skill
import com.origin.model.inventory.ItemType as it
import com.origin.model.inventory.ItemWithCount as item

class Craft(
    val name: String,
    val produce: List<item>,
    val required: List<item>,
    val ticksPerStep: Int = 2,
    val steps: Int = 10,
    val staminaCost: Double = 2.0,
    val skills: Set<Skill.Type>? = null,
) {
    companion object {
        fun findByName(name: String): Craft? {
            return craftList.find { it.name == name }
        }
    }

    fun getHumanReadableName(): String {
        return name.replace("_", " ").lowercase()
    }
}

val craftList = listOf(
    Craft(
        "STONE_AXE", listOf(item(it.STONE_AXE)),
        listOf(item(it.STONE), item(it.BRANCH, 2)),
    ),
    Craft(
        "IRON_AXE", listOf(item(it.STONE_AXE)),
        listOf(item(it.APPLE, 3), item(it.BRANCH, 2)),
    ),
    Craft(
        "BUCKET", listOf(item(it.BUCKET)),
        listOf(item(it.BOARD, 2)),
        2, 10,
        2.0,
        setOf(Skill.Type.CARPENTRY)
    ),
)
