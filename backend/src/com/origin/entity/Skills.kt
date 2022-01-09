package com.origin.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object Skills : LongIdTable("skills") {
    val character = reference("character", Characters)
    val skillId = integer("skill_id")

    init {
        uniqueIndex(character, skillId)
    }
}

class Skill(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Skill>(Skills)

    val character by Character referrersOn Skills.character
    val skillId by Skills.skillId
}