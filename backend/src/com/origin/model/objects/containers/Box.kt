package com.origin.model.objects.containers

import com.origin.entity.EntityObject
import com.origin.utils.Rect
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class Box(entity: EntityObject) : Container(entity) {

    override val normalResource = "box/normal"

    override val openResource = "box/open"

    override fun getBoundRect(): Rect {
        return Rect(6)
    }
}