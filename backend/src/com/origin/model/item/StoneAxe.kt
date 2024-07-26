package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord

class StoneAxe(record: InventoryRecord) : Item(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            (ItemFactory.add(16, StoneAxe::class.java as Class<Item>))
        }
    }

    override fun icon(): String {
        return "/items/stone_axe.png"
    }
}