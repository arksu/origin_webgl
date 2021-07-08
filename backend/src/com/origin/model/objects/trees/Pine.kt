package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import com.origin.utils.Rect
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class Pine(entity: EntityObject) : Tree(entity) {
    override fun getResourcePath(): String {
        if (stage == 10) return "trees/pine/stump"
        return "trees/pine/$stage"
    }

    override fun getBoundRect(): Rect {
        return Rect(10)
    }
}
