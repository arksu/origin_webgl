package com.origin.model.`object`

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.GameObject

object ObjectsFactory {
    fun constructByRecord(record: ObjectRecord): GameObject {
        return when (record.type) {
            else -> UnknownObject(record)
        }
    }
}