package com.origin.model.skills

import com.origin.model.inventory.ItemType

/**
 * игровой скилл
 */
enum class Skill(
    val id: Int
) {
    LUMBERJACKING(1),
    CARPENTRY(2),
    POTTERY(3),
    FISHING(4);

    companion object {
        private val map = values().associateBy(Skill::id)
        fun fromId(id: Int) = map[id] ?: throw IllegalArgumentException("skill not found $id")
    }
}
