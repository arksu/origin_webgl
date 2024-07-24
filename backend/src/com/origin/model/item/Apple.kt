package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord

class Apple(record: InventoryRecord) : Food(record) {
    companion object {
        init {
            println("1a")
        }
    }
}