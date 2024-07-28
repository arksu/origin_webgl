package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord

class RabbitDead(record: InventoryRecord) : Item(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            ItemFactory.add(23, RabbitDead::class.java as Class<Item>, "/items/rabbit_dead.png")
        }
    }

    override val width = 1
    override val height = 2
}