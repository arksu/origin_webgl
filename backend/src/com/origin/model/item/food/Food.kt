package com.origin.model.item.food

import com.origin.jooq.tables.records.InventoryRecord
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.item.Item

abstract class Food(record: InventoryRecord) : Item(record) {

    override fun getContextMenu(player: Player): ContextMenu? {
        val list = LinkedHashSet<String>()
        if (catEat()) {
            list.add("Eat")
        }
        return if (list.isNotEmpty()) ContextMenu(this, list) else null
    }

    override suspend fun executeContextMenuItem(player: Player, selected: String) {
        when (selected) {
            "Eat" -> {
                eat()
            }
        }
    }

    open fun catEat(): Boolean {
        return true
    }

    open suspend fun eat() {
        // TODO FEP, etc...
    }
}