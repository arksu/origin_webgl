package com.origin

import com.origin.entity.EntityObject
import com.origin.model.GameObject
import com.origin.model.StaticObject
import com.origin.model.trees.Birch
import com.origin.model.trees.Fir
import com.origin.model.trees.Pine
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
object Const {
    fun getObjectByType(entity: EntityObject): GameObject {
        return when (entity.type) {
            1 -> Birch(entity)
            2 -> Fir(entity)
            3 -> Pine(entity)
            else -> StaticObject(entity)
        }
    }
}