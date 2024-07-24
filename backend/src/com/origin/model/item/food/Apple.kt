package com.origin.model.item.food

import com.origin.jooq.tables.records.InventoryRecord
import com.origin.model.item.Item
import com.origin.model.item.ItemFactory

class Apple(record: InventoryRecord) : Food(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            (ItemFactory.add(15, Apple::class.java as Class<Item>))
        }
    }

    override val icon = "/items/apple.png"
}