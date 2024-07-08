package com.origin.model.`object`

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.GameObject
import com.origin.model.`object`.tree.Birch

object ObjectsFactory {
    fun constructByRecord(record: ObjectRecord): GameObject {
        return when (record.type) {
            1 -> Birch(record)
            else -> UnknownObject(record)
        }
    }
}