package com.origin.entity

import org.jetbrains.exposed.sql.Table

object Skills : Table("skills") {
    val characterId = long("character_id").references(Characters.id)
    val skillId = integer("skill_id")
    val level = integer("level").default(1)

    init {
        // нельзя добавить один и тот же скилл несколько раз одному игроку
        uniqueIndex(characterId, skillId)
    }
}
