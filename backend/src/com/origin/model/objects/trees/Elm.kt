package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Вяз
 */
@ObsoleteCoroutinesApi
class Elm(entity: EntityObject) : Tree(entity) {

    override fun getResourcePath(): String {
        if (stage == 10) return "trees/elm/stump"
        return "trees/elm/$stage"
    }
}