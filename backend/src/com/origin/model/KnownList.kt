package com.origin.model

import com.origin.net.model.ObjectAdd
import com.origin.net.model.ObjectDel
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * список игровых объектов о которых знает другой объект
 * то есть эти объекты проецируются на клиент, список всех видимых объектов с точки зрения клиента
 */
@ObsoleteCoroutinesApi
class KnownList(private val activeObject: GameObject) {

    private val knownObjects = HashMap<ObjectID, GameObject>()

    private val knownPlayers = HashMap<ObjectID, Player>()

    /**
     * известенли этот объект?
     */
    fun isKnownObject(obj: GameObject): Boolean {
        return knownObjects.containsKey(obj.id)
    }

    suspend fun addKnownObject(obj: GameObject): Boolean {
        if (isKnownObject(obj)) return false

        val result = knownObjects.put(obj.id, obj) == null
        if (result && obj is Player) {
            knownPlayers[obj.id] = obj
        }
        if (result && activeObject is Player) {
            activeObject.session.send(ObjectAdd(obj))
        }
        return result
    }

    suspend fun removeKnownObject(obj: GameObject): Boolean {
        val result = knownObjects.remove(obj.id) != null
        if (result && obj is Player) {
            knownPlayers.remove(obj.id)
        }
        if (result && activeObject is Player) {
            activeObject.session.send(ObjectDel(obj))
        }
        return result
    }
}