package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.`object`.ObjectsFactory

class Oak(objectRecord: ObjectRecord) : Tree(objectRecord) {
    companion object {
        init {
            ObjectsFactory.add(6, Oak::class.java)
        }
    }

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/oak/stump"
        return "trees/oak/$stage"
    }

    override val logs = 1
}