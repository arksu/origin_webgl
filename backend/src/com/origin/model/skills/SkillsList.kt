package com.origin.model.skills

import com.origin.entity.Skills
import com.origin.model.Player
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * работа со скиллами игрока
 */
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class SkillsList(val player: Player) {
    val list: MutableList<Skill> = ArrayList()

    init {
        transaction {
            // загрузим все скиллы нашего персонажа
            Skills.select { Skills.characterId eq this@SkillsList.player.id }.forEach {
                list.add(Skill(Skill.fromId(it[Skills.skillId]), it[Skills.level]))
            }
        }
    }

    /**
     * добавить скилл игроку
     */
    fun add(skill: Skill) {
        transaction {
            // проверим что такого скилла еще нет в списке
            if (!contains(skill.type)) {
                Skills.insert {
                    it[characterId] = player.id
                    it[skillId] = skill.type.id
                    it[level] = skill.level
                }
                list.add(skill)
            }
        }
    }

    /**
     * есть ли указанный скилл у игрока?
     */
    fun contains(t: Skill.Type): Boolean {
        return list.any { it.type == t }
    }
}