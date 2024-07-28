package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord
import com.origin.model.craft.Craft
import com.origin.model.craft.CraftFactory

class StoneAxe(record: InventoryRecord) : Axe(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            ItemFactory.add(16, StoneAxe::class.java as Class<Item>, "/items/stone_axe.png")

            CraftFactory.add(
                Craft(
                    name = "Stone Axe",
                    ticks = 15,
                    staminaConsume = 100,
                    minimumStaminaRequired = 200,
                    produce = mapOf(StoneAxe::class.java to 1),
                    requiredItems = mapOf(
                        Stone::class.java to 1,
                        Branch::class.java to 2
                    ),
                    requiredSkills = setOf()
                )
            )
        }
    }
}