package com.origin.model.craft

import com.origin.model.Skill

class Craft(
    val produce: Class<*>,
    val requiredItems: Map<Class<*>, Int>,
    val requiredSkills: Collection<Skill>
) {

}