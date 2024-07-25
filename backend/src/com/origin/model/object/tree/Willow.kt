package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.`object`.ObjectsFactory

/**
 * ива
 */
class Willow(objectRecord: ObjectRecord) : Tree(objectRecord) {

    companion object {
        init {
            ObjectsFactory.add(10, Willow::class.java)
        }
    }

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/willow/stump"
        return "trees/willow/$stage"
    }

    override val maxBranch = 8
}