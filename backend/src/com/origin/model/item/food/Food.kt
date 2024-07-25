package com.origin.model.item.food

import com.origin.jooq.tables.records.InventoryRecord
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.item.Item

abstract class Food(record: InventoryRecord) : Item(record) {
    override fun getContextMenu(player: Player): ContextMenu? {
        return ContextMenu(this, setOf("Eat"))
    }

    override fun executeContextMenuItem(player: Player, selected: String) {
        when (selected) {
            "Eat" -> {
                println("eat")
            }
        }
    }
}