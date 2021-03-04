package com.origin.model.objects

import com.origin.entity.EntityObject
import com.origin.model.GameObject
import com.origin.model.objects.containers.Box
import com.origin.model.objects.trees.*
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
object ObjectsFactory {

    fun byEntity(entity: EntityObject): GameObject {
        return when (entity.type) {
            1 -> Birch(entity)
            2 -> Fir(entity)
            3 -> Pine(entity)
            4 -> Box(entity)
            5 -> Apple(entity)
            6 -> Oak(entity)
            else -> UnknownObject(entity)
        }
    }
}