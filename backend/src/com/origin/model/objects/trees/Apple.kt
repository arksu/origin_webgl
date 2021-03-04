package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class Apple(entity: EntityObject) : Tree(entity) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/apple/stump"
        return "trees/apple/$stage"
    }
}