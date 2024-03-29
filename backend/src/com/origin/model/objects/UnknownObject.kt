package com.origin.model.objects

import com.origin.entity.EntityObject
import com.origin.model.StaticObject
import com.origin.model.inventory.Inventory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
class UnknownObject(entity: EntityObject) : StaticObject(entity) {
    override val inventory: Inventory? = null

    override fun getResourcePath(): String {
        return "unknown"
    }
}
