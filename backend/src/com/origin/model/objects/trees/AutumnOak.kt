package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Осенний Дуб
 */

@ObsoleteCoroutinesApi
class AutumnOak(entity: EntityObject) : Tree(entity) {
    override fun getResourcePath(): String {
        if (stage == 10) return "trees/autumn_oak/stump"
        return "trees/autumn_oak/$stage"
    }
}
