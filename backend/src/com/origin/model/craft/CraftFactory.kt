package com.origin.model.craft

import com.origin.CraftList
import com.origin.model.Player
import com.origin.model.Skill

object CraftFactory {
    private val map = HashMap<String, Craft>()

    fun add(
        name: String,
        produce: Map<Class<*>, Int>, requiredItems: Map<Class<*>, Int>, requiredSkills: Collection<Skill>
    ) {
        map[name] = Craft(name, produce, requiredItems, requiredSkills)
    }

    /**
     * получить список доступных крафтов для игрока
     */
    fun forPlayer(player: Player, result: HashMap<String, Craft> = HashMap()): CraftList {
        // проверяем доступные скиллы у игрока для каждого крафта
        map.forEach { (name, craft) ->
            if (craft.requiredSkills.isEmpty() || player.skills.contains(craft.requiredSkills)) {
                result[name] = craft
            }
        }
        return result
    }
}