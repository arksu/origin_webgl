package com.origin.model.objects

import com.origin.entity.EntityObject
import com.origin.model.StaticObject
import com.origin.utils.Rect
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class Box(entity: EntityObject) : StaticObject(entity) {
    override fun getResourcePath(): String {
        return "box/norm"
    }

    override fun getBoundRect(): Rect {
        return Rect(6)
    }
}