package com.origin.model.objects.containers

import com.origin.entity.EntityObject
import com.origin.utils.Rect
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * ящик
 */

@ObsoleteCoroutinesApi
class Crate(entity: EntityObject) : Container(entity) {

    override val normalResource = "crate/1"

    override val openResource = "crate/2"

    override fun getBoundRect(): Rect {
        return Rect(6)
    }
}