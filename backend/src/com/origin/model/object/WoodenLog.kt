package com.origin.model.`object`

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.StaticObject
import com.origin.util.Rect

class WoodenLog(record: ObjectRecord) : StaticObject(record) {
    override val inventory = null

    override fun getResourcePath(): String {
        return "log"
    }

    override fun getBoundRect(): Rect {
        return Rect(0)
    }

}