package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.`object`.ObjectsFactory

/**
 * Тис
 */
class Yew(objectRecord: ObjectRecord) : Tree(objectRecord) {
    companion object {
        init {
            ObjectsFactory.add(11, Yew::class.java)
        }
    }

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/yew/stump"
        return "trees/yew/$stage"
    }

    override val maxBranch = 8
}