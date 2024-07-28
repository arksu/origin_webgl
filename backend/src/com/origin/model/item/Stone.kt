package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord

class Stone(record: InventoryRecord) : Item(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            ItemFactory.add(17, Stone::class.java as Class<Item>, "/items/stone.png")
        }
    }
}