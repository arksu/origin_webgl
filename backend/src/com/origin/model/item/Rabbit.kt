package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord

class Rabbit(record: InventoryRecord) : Item(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            (ItemFactory.add(19, Rabbit::class.java as Class<Item>))
        }
    }

    override val width = 2
    override val height = 2

    override fun icon(): String {
        return "/items/rabbit.png"
    }
}