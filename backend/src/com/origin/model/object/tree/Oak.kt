package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord

class Oak(objectRecord: ObjectRecord) : Tree(objectRecord) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/oak/stump"
        return "trees/oak/$stage"
    }
}