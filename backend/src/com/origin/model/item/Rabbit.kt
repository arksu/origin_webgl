package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord
import com.origin.model.ContextMenu
import com.origin.model.Player

class Rabbit(record: InventoryRecord) : Item(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            ItemFactory.add(19, Rabbit::class.java as Class<Item>, "/items/rabbit.png")
        }
    }

    override val width = 2
    override val height = 2

    override fun getContextMenu(player: Player): ContextMenu? {
        val list = LinkedHashSet<String>()
        list.add("Wring Neck")
        return if (list.isNotEmpty()) ContextMenu(this, list) else null
    }

    override suspend fun executeContextMenuItem(player: Player, selected: String) {
        when (selected) {
            "Wring Neck" -> {
                inventory?.replace(ItemFactory.create(RabbitDead::class.java, this))
            }
        }
    }
}