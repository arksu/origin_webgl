package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.`object`.ObjectsFactory

/**
 * Клен
 */
class Maple(objectRecord: ObjectRecord) : Tree(objectRecord) {

    companion object {
        init {
            ObjectsFactory.add(9, Maple::class.java)
        }
    }

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/maple/stump"
        return "trees/maple/$stage"
    }

    override val maxBranch = 10
    override val logs = 3
}