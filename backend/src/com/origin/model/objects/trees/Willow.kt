package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Ива
 */
@ObsoleteCoroutinesApi
class Willow(entity: EntityObject) : Tree(entity) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/willow/stump"
        return "trees/willow/$stage"
    }
}
