package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord

class Apple(objectRecord: ObjectRecord) : Tree(objectRecord) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/apple/stump"
        return "trees/apple/$stage"
    }

    override val logs = 1
}