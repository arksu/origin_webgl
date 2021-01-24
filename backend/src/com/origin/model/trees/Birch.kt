package com.origin.model.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Береза
 */
@ObsoleteCoroutinesApi
class Birch(entity: EntityObject) : Tree(entity) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/birch/stump"
        return "trees/birch/$stage"
    }
}