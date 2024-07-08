package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.StaticObject
import com.origin.model.inventory.Inventory
import com.origin.util.Rect

abstract class Tree(record: ObjectRecord) : StaticObject(record) {
    /**
     * стадия роста
     * если есть данные объекта то это и есть номер стадии.
     * если нет данных ставим по дефолту 6 стадию роста
     */
    var stage: Int = record.data?.toInt() ?: 6

    override val inventory: Inventory? = null

    override fun getBoundRect(): Rect {
        return Rect(6)
    }
}