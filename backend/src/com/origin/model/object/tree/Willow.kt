package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord

/**
 * ива
 */
class Willow(objectRecord: ObjectRecord) : Tree(objectRecord) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/willow/stump"
        return "trees/willow/$stage"
    }
}