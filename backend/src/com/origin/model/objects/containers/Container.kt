package com.origin.model.objects.containers

import com.origin.entity.EntityObject
import com.origin.model.BroadcastEvent
import com.origin.model.Human
import com.origin.model.StaticObject
import com.origin.model.inventory.Inventory
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
abstract class Container(entity: EntityObject) : StaticObject(entity) {
    override val inventory by lazy { Inventory(this) }

    private val discoverers = HashMap<ObjectID, Human>()

    abstract fun getNormalResource(): String

    abstract fun getOpenResource(): String

    override fun getResourcePath(): String {
        return if (discoverers.size > 0) getOpenResource() else getNormalResource()
    }

    override suspend fun openBy(who: Human) {
        super.openBy(who)
        val oldSize = discoverers.size
        discoverers[who.id] = who
        if (oldSize == 0) {
            println("OPEN")
            grid.broadcast(BroadcastEvent.Changed(this))
        }
    }

    override suspend fun closeBy(who: Human) {
        super.closeBy(who)
        discoverers.remove(who.id)
        if (discoverers.size == 0) {
            println("CLOSE")
            grid.broadcast(BroadcastEvent.Changed(this))
        }
    }
}