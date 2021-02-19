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
                return true
            }
        return false
    }

    fun get(id: ObjectID): GameObject? {
        return list[id]
    }

    suspend fun close(id: ObjectID) {
        if (list.containsKey(id)) {
            logger.warn("close $id")
            list.remove(id)
            if (me is Player) {
                me.session.send(InventoryClose(id))
            }
        }
    }

    suspend fun closeAll() {
        if (list.size > 0) {
            logger.warn("closeAll")
            list.values.forEach {
                if (me is Player) {
                    me.session.send(InventoryClose(it.id))
                }
            }
            list.clear()
        }
    }
}