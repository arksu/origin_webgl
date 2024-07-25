package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.`object`.ObjectsFactory

/**
 * Ель
 */
class Fir(objectRecord: ObjectRecord) : Tree(objectRecord) {

    companion object {
        init {
            ObjectsFactory.add(3, Fir::class.java)
        }
    }

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/fir/stump"
        return "trees/fir/$stage"
    }

    override val maxBranch = 7
    override val logs = 1
}