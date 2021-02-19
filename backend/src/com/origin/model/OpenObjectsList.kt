package com.origin.model

import com.origin.net.model.InventoryClose
import com.origin.net.model.InventoryUpdate
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val OPEN_DISTANCE = 3

/**
 * список "открытых" объектов для взаимодействия с их инвентарями
 */
@ObsoleteCoroutinesApi
class OpenObjectsList(private val me: Human) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(OpenObjectsList::class.java)
    }

    private val list = HashMap<ObjectID, GameObject>()

    suspend fun open(obj: GameObject): Boolean {
        val inv = obj.inventory
        if (inv != null)
            if (!list.containsKey(obj.id)) {
                logger.warn("open $obj")
                list[obj.id] = obj
                if (me is Player) {
                    me.session.send(InventoryUpdate(inv))
                }
                obj.send(GameObjectMsg.OpenBy(me))
                return true
            }
        return false
    }

    fun get(id: ObjectID): GameObject? {
        return list[id]
    }

    suspend fun close(id: ObjectID) {
        val obj = list.remove(id)
        if (obj != null) {
            if (me is Player) {
                me.session.send(InventoryClose(id))
            }
            obj.send(GameObjectMsg.CloseBy(me))
        }
    }

    suspend fun closeAll() {
        if (list.size > 0) {
            list.values.forEach {
                if (me is Player) {
                    me.session.send(InventoryClose(it.id))
                }
                it.send(GameObjectMsg.CloseBy(me))
            }
            list.clear()
        }
    }
}