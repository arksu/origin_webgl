package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Клен
 */
@ObsoleteCoroutinesApi
class Maple(entity: EntityObject) : Tree(entity) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/maple/stump"
        return "trees/maple/$stage"
    }
}