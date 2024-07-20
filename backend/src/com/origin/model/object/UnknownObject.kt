package com.origin.model.`object`

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.StaticObject

class UnknownObject(record: ObjectRecord) : StaticObject(record) {

    override fun getResourcePath(): String {
        return "unknown"
    }
}
