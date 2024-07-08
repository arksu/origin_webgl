package com.origin.model.`object`.container

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.util.Rect

class Box(record: ObjectRecord) : Container(record) {

    override val normalResource = "box/normal"

    override val openResource = "box/open"

    override fun getBoundRect(): Rect {
        return Rect(6)
    }
}
