package com.origin.model.item

import com.origin.jooq.tables.records.InventoryRecord

class Board(record: InventoryRecord) : Item(record) {
    companion object {
        init {
            @Suppress("UNCHECKED_CAST")
            ItemFactory.add(20, Board::class.java as Class<Item>, "/items/board.png")
        }
    }

    override val width = 1
    override val height = 4
}