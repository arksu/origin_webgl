package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord

class Birch(record: ObjectRecord) : Tree(record) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/birch/stump"
        return "trees/birch/$stage"
    }

    override val maxBranch = 7
    override val logs = 2
}