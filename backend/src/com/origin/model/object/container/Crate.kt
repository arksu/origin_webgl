package com.origin.model.`object`.container

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.`object`.ObjectsFactory
import com.origin.util.Rect

/**
 * ящик
 */
class Crate(record: ObjectRecord) : Container(record) {
    companion object {
        init {
            ObjectsFactory.add(12, Crate::class.java)
        }
    }

    override val normalResource = "crate/empty"

    override val openResource = "crate/full"

    override fun getBoundRect(): Rect {
        return Rect(6)
    }
}
