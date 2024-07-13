package com.origin.model

import com.origin.ObjectID
import com.origin.model.`object`.container.ContainerMessage
import com.origin.net.InventoryClose
import com.origin.net.InventoryUpdate
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * список "открытых" объектов для взаимодействия с их инвентарями
 */
class OpenedObjectsList(private val me: Human) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(OpenedObjectsList::class.java)
    }

    /**
     * список объектов которые я открыл, можно открыть сразу несколько объектов
     */
    private val map = HashMap<ObjectID, GameObject>()

    /**
     * открыть объект (инвентарь)
     */
    suspend fun open(obj: GameObject): Boolean {
        val inv = obj.inventory
        if (inv != null)
            if (!map.containsKey(obj.id)) {
                logger.warn("open $obj")
                map[obj.id] = obj
                if (me is Player) {
                    me.session.send(InventoryUpdate(inv))
                }
                obj.send(ContainerMessage.OpenBy(me))
                return true
            }
        return false
    }

    fun get(id: ObjectID): GameObject? {
        return map[id]
    }

    /**
     * закрыть объект (инвентарь)
     */
    suspend fun close(id: ObjectID) {
        val obj = map.remove(id)
        if (obj != null) {
            if (me is Player) {
                me.session.send(InventoryClose(id))
            }
            obj.send(ContainerMessage.CloseBy(me))
        }
    }

    /**
     * закрыть все объекты с которыми взаимодействовали
     */
    suspend fun closeAll() {
        if (map.size > 0) {
            map.values.forEach {
                if (me is Player) {
                    me.session.send(InventoryClose(it.id))
                }
                it.send(ContainerMessage.CloseBy(me))
            }
            map.clear()
        }
    }
}
