package com.origin.model.`object`.container

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.util.Rect

/**
 * ящик
 */
class Crate(record: ObjectRecord) : Container(record) {

    override val normalResource = "crate/empty"

    override val openResource = "crate/full"

    override fun getBoundRect(): Rect {
        return Rect(6)
    }
}
