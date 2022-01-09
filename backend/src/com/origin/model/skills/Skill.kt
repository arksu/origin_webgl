package com.origin.model.skills

/**
 * игровой скилл
 */
class Skill(
    val type: Type,
    var level: Int
) {
    enum class Type(val id: Int) {
        LUMBERJACKING(1),
        CARPENTRY(2),
        POTTERY(3),
        FISHING(4);
    }

    companion object {
        private val map = Type.values().associateBy(Type::id)
        fun fromId(id: Int) = map[id] ?: throw IllegalArgumentException("skill not found $id")
    }
}
