package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Орешник
 */
@ObsoleteCoroutinesApi
class Hazel(entity: EntityObject) : Tree(entity) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/hazel/stump"
        return "trees/hazel/$stage"
    }
}
