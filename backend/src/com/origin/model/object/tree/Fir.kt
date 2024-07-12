package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord

/**
 * Ель
 */
class Fir(objectRecord: ObjectRecord) : Tree(objectRecord) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/fir/stump"
        return "trees/fir/$stage"
    }
}