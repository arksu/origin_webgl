package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord

abstract class Food(record: InventoryRecord) : Item(record) {
}