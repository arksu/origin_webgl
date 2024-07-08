package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.inventory.Inventory

class Birch(objectRecord: ObjectRecord) : Tree(objectRecord) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/birch/stump"
        return "trees/birch/$stage"
    }

}