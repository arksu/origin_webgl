package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord

class Bark(record: InventoryRecord) : Item(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            (ItemFactory.add(21, Bark::class.java as Class<Item>))
        }
    }

    override fun icon(): String {
        return "/items/bark.png"
    }
}