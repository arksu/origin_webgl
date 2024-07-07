package com.origin.model.`object`

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.StaticObject
import com.origin.model.inventory.Inventory

class UnknownObject(record: ObjectRecord) : StaticObject(record) {
    override val inventory: Inventory? = null

    override fun getResourcePath(): String {
        return "unknown"
    }
}
