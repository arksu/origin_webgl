package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord

class Branch(record: InventoryRecord) : Item(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            ItemFactory.add(18, Branch::class.java as Class<Item>, "/items/branch.png")
        }
    }
}