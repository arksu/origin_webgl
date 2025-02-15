package com.origin.model.item.food

import com.origin.jooq.tables.records.InventoryRecord
import com.origin.model.item.Item
import com.origin.model.item.ItemFactory

class Apple(record: InventoryRecord) : Food(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            ItemFactory.add(15, Apple::class.java as Class<Item>, "/items/apple.png")
        }
    }

    override fun getIcon(): String {
        return if (record.data == "1") "/items/apple_core.png" else super.getIcon()
    }

    override fun canEat(): Boolean {
        // 1=значит уже съели. и это огрызок
        return record.data != "1"
    }

    override suspend fun eat() {
        super.eat()
        record.data = "1"
        save()
        inventory?.notify()

        // TODO FEP, etc...
    }
}