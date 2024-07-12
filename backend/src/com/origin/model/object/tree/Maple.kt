package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord

/**
 * Клен
 */
class Maple(objectRecord: ObjectRecord) : Tree(objectRecord) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/maple/stump"
        return "trees/maple/$stage"
    }
}