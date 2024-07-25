package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.`object`.ObjectsFactory

/**
 * орешник
 */
class Hazel(objectRecord: ObjectRecord) : Tree(objectRecord) {

    companion object {
        init {
            ObjectsFactory.add(8, Hazel::class.java)
        }
    }

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/hazel/stump"
        return "trees/hazel/$stage"
    }

    override val maxBranch = 8
    override val logs = 1
}