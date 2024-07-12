package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord

/**
 * орешник
 */
class Hazel(objectRecord: ObjectRecord) : Tree(objectRecord) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/hazel/stump"
        return "trees/hazel/$stage"
    }
}