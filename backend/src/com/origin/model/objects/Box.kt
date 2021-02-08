package com.origin.model.objects

import com.origin.entity.EntityObject
import com.origin.model.StaticObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class Box(entity: EntityObject) : StaticObject(entity) {
    override fun getResourcePath(): String {

        return "box/norm"
    }
}