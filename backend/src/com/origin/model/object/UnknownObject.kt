package com.origin.model.`object`

import com.origin.jooq.tables.records.ObjectRecord

class UnknownObject(record: ObjectRecord) : StaticObject(record) {

    override fun getResourcePath(): String {
        return "unknown"
    }
}
