package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.`object`.ObjectsFactory

class Apple(objectRecord: ObjectRecord) : Tree(objectRecord) {

    companion object {
        init {
            ObjectsFactory.add(5, Apple::class.java)
        }
    }

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/apple/stump"
        return "trees/apple/$stage"
    }

    override val logs = 1
}