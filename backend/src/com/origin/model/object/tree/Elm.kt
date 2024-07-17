package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord

/**
 * Вяз
 */
class Elm(objectRecord: ObjectRecord) : Tree(objectRecord) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/elm/stump"
        return "trees/elm/$stage"
    }

    override val maxBranch = 16
    override val logs = 1
}