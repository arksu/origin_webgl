package com.origin.model

import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.references.SKILL

class SkillsList(val player: Player) {

    val list = ArrayList<Skill>()

    init {
        load()
    }

    private fun load() {
        val l = DatabaseConfig.dsl
            .selectFrom(SKILL)
            .where(SKILL.CHARACTER_ID.eq(player.id))
            .fetch()
        l.forEach { record ->
            list.add(
                Skill(
                    type = Skill.byTypeId(record.skillId),
                    level = record.level
                )
            )
        }
    }

    /**
     * есть ли такой скилл?
     */
    fun have(type: Skill.Type): Boolean {
        return list.any { it.type == type }
    }

    /**
     * добавить скилл игроку
     */
    fun add(type: Skill.Type) {
        if (have(type)) return

        val skill = Skill(type, 1)
        list.add(skill)
        DatabaseConfig.dsl
            .insertInto(SKILL)
            .set(SKILL.CHARACTER_ID, player.id)
            .set(SKILL.SKILL_ID, type.id)
            .set(SKILL.LEVEL, skill.level)
            .execute()
    }

    fun contains(required: Collection<Skill>): Boolean {
        return list.containsAll(required)
    }
}