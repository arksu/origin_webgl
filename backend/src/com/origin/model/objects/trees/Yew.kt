package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Тис
 */
@ObsoleteCoroutinesApi
class Yew(entity: EntityObject) : Tree(entity) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/yew/stump"
        return "trees/yew/$stage"
    }
}
