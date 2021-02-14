package com.origin.model

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

    fun open(obj: GameObject) {
        if (!list.containsKey(obj.id)) {
            logger.warn("open $obj")
            list.put(obj.id, obj)
        }
    }

    fun close(obj: GameObject) {
        if (list.containsKey(obj.id)) {
            logger.warn("close $obj")
            list.remove(obj.id)
        }
    }

    fun closeAll() {
        if (list.size > 0) {
            logger.warn("closeAll")
            list.values.forEach {

            }
            list.clear()
        }
    }
}