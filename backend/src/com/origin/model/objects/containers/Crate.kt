package com.origin.model.objects.containers

import com.origin.entity.EntityObject
import com.origin.utils.Rect
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * ящик
 */
@ObsoleteCoroutinesApi
class Crate(entity: EntityObject) : Container(entity) {

    override val normalResource = "crate/empty"

    override val openResource = "crate/full"

    override fun getBoundRect(): Rect {
        return Rect(6)
    }
}
