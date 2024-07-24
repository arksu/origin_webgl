package com.origin.model.item.food

import com.origin.jooq.tables.records.InventoryRecord
import com.origin.model.item.Item

abstract class Food(record: InventoryRecord) : Item(record) {
}