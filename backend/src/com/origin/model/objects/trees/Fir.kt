package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Ель
 */
@ObsoleteCoroutinesApi
class Fir(entity: EntityObject) : Tree(entity) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/fir/stump"
        return "trees/fir/$stage"
    }
}