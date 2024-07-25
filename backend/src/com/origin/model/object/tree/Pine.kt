package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.`object`.ObjectsFactory
import com.origin.util.Rect

class Pine(objectRecord: ObjectRecord) : Tree(objectRecord) {
    companion object {
        init {
            ObjectsFactory.add(4, Pine::class.java)
        }
    }

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/pine/stump"
        return "trees/pine/$stage"
    }

    override fun getBoundRect(): Rect {
        return Rect(10)
    }

    override val maxBranch = 3
    override val logs = 3
}