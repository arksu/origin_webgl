package com.origin.model.`object`

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.StaticObject
import com.origin.util.Rect

class Stone(record: ObjectRecord) : StaticObject(record) {
    companion object {
        init {
            ObjectsFactory.add(13, Stone::class.java)
        }
    }

    override fun getResourcePath(): String {
        return "stone"
    }

    override fun getBoundRect(): Rect {
        return Rect(6)
    }

}