package com.origin.model.trees

import com.origin.entity.EntityObject
import com.origin.model.StaticObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Деревья
 */
@ObsoleteCoroutinesApi
open class Tree(entity: EntityObject) : StaticObject(entity) {
    var stage: Int = entity.data?.toInt() ?: 6
}