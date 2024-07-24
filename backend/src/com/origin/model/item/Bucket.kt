package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord

class Bucket(record: InventoryRecord) : Item(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            (ItemFactory.add(22, Bucket::class.java as Class<Item>))
        }
    }

    override val width = 2
    override val height = 2
    override val icon = "/items/bucket_empty.png"
}