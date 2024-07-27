package com.origin.model.craft

import com.origin.model.Skill

object CraftFactory {
    val map = HashMap<String, Craft>()

    fun add(name: String, produce: Class<*>, requiredItems: Map<Class<*>, Int>, requiredSkills: Collection<Skill>) {
        map[name] = Craft(produce, requiredItems, requiredSkills)
    }
}