package com.origin.model

import com.origin.ObjectID
import com.origin.net.InventoryClose
import com.origin.net.InventoryUpdate
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * список "открытых" объектов для взаимодействия с их инвентарями
 */
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
                obj.send(GameObjectMessage.OpenBy(me))
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
            obj.send(GameObjectMessage.CloseBy(me))
        }
    }

    suspend fun closeAll() {
        if (list.size > 0) {
            list.values.forEach {
                if (me is Player) {
                    me.session.send(InventoryClose(it.id))
                }
                it.send(GameObjectMessage.CloseBy(me))
            }
            list.clear()
        }
    }
}
