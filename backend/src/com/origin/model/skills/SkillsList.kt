package com.origin.model.skills

import com.origin.entity.Characters
import com.origin.entity.Skills
import com.origin.model.Player
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

/**
 * работа со скиллами игрока
 */
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class SkillsList(val player: Player) {
    val list: MutableSet<Skill> = TreeSet()

    init {
        transaction {
            // загрузим все скиллы нашего персонажа
            Skills.select { Skills.characterId eq this@SkillsList.player.id }.forEach {
                list.add(Skill.fromId(it[Skills.skillId]))
            }
        }
    }

    /**
     * добавить скилл игроку
     */
    fun add(skill: Skill) {
        transaction {
            // проверим что такого скилла еще нет в списке
            if (!list.contains(skill)) {
                Skills.insert {
                    it[characterId] = player.id
                    it[skillId] = skill.id
                }
                list.add(skill)
            }
        }
    }
}