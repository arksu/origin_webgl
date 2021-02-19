package com.origin.model.objects.containers

import com.origin.entity.EntityObject
import com.origin.utils.Rect
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class Box(entity: EntityObject) : Container(entity) {

    override fun getNormalResource(): String {
        return "box/normal"
    }

    override fun getOpenResource(): String {
        return "box/open"
    }

    override fun getBoundRect(): Rect {
        return Rect(6)
    }
}