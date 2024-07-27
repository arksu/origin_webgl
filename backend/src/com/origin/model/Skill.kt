package com.origin.model

class Skill(
    val type: Type,
    var level: Int
) {
    companion object {
        private val map = Type.entries.associateBy(Type::id)
        fun fromId(id: Int) = map[id] ?: throw IllegalArgumentException("skill not found $id")
    }

    enum class Type(val id: Int) {
        LUMBERJACKING(1),
        CARPENTRY(2),
        POTTERY(3),
        FISHING(4),
        SWIMMING(5),
        STONE_WORKING(6),
        FARMING(7),
        HEARTH_MAGIC(8),
        TANNING(9),
        ARCHERY(10),
        MINING(11),
        SEWING(12),
        BEEKEEPING(13),
        WINEMAKING(14),
        YEOMANRY(15),
        TRESPASSING(16),
        BOAT_BUILDING(17),
        LANDSCAPING(18),

    }
}