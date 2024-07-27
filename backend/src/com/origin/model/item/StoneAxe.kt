package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord
import com.origin.model.craft.CraftFactory

class StoneAxe(record: InventoryRecord) : Item(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            (ItemFactory.add(16, StoneAxe::class.java as Class<Item>))

            CraftFactory.add(
                name = "Stone Axe",
                icon = "/items/stone_axe.png",
                produce = mapOf(StoneAxe::class.java to 1),
                requiredItems = mapOf(
                    Stone::class.java to 1,
                    Branch::class.java to 2
                ),
                requiredSkills = setOf()
            )
        }
    }

    override fun icon(): String {
        return "/items/stone_axe.png"
    }
}