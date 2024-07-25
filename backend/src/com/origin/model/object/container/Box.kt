package com.origin.model.`object`.container

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.`object`.ObjectsFactory
import com.origin.util.Rect

class Box(record: ObjectRecord) : Container(record) {
    companion object {
        init {
            ObjectsFactory.add(1, Box::class.java)
        }
    }

    override val normalResource = "box/normal"

    override val openResource = "box/open"

    override fun getBoundRect(): Rect {

        return Rect(6)
    }
}
