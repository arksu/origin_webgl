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

    override fun icon(): String {
        return if (record.data == "1") "/items/apple_core.png" else "/items/apple.png"
    }

    override fun catEat(): Boolean {
        return record.data != "1"
    }

    override suspend fun eat() {
        super.eat()
        record.data = "1"
        save()
        inventory?.notify()
    }
}