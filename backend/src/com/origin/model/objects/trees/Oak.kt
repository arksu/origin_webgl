package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class Oak(entity: EntityObject) : Tree(entity) {
    override fun getResourcePath(): String {
        if (stage == 10) return "trees/oak/stump"
        return "trees/oak/$stage"
    }
}
