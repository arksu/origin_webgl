package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord

/**
 * Тис
 */
class Yew(objectRecord: ObjectRecord) : Tree(objectRecord) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/yew/stump"
        return "trees/yew/$stage"
    }

    override val maxBranch = 8
}