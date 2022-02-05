package com.origin.model

import com.origin.net.model.ObjectAdd
import com.origin.net.model.ObjectDel
import com.origin.utils.ObjectID
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * список игровых объектов о которых знает другой объект,
 * то есть эти объекты проецируются на клиент, список всех видимых объектов с точки зрения клиента
 */
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
class KnownList(private val me: GameObject) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(KnownList::class.java)
    }

    private val knownObjects = HashMap<ObjectID, GameObject>()

    private val knownPlayers = HashMap<ObjectID, Player>()

    /**
     * известенли этот объект?
     */
    fun isKnownObject(obj: GameObject): Boolean {
        return knownObjects.containsKey(obj.id)
    }

    /**
     * получить объект по его ид
     * @return null если объект не известен
     */
    fun getKnownObject(id: ObjectID): GameObject? {
        return knownObjects[id]
    }

    /**
     * добавить объект в список известных мне
     * @return true если объект еще НЕ был известен мне
     */
    suspend fun addKnownObject(obj: GameObject): Boolean {
        if (isKnownObject(obj)) return false

        knownObjects[obj.id] = obj

        if (obj is Player) {
            knownPlayers[obj.id] = obj
        }
        if (me is Player) {
            logger.debug("object add ${obj.pos}")
            me.session.send(ObjectAdd(obj))
        }

        return true
    }

    /**
     * удалить объект из списка известных мне
     * @return true если объект был известен мне
     */
    suspend fun removeKnownObject(obj: GameObject): Boolean {
        val result = knownObjects.remove(obj.id) != null
        if (result) {
            if (obj is Player) {
                knownPlayers.remove(obj.id)
            }
            if (me is Player) {
                me.session.send(ObjectDel(obj))
            }
        }
        return result
    }

    private fun getKnownObjects(): Collection<GameObject> {
        return knownObjects.values
    }

    /**
     * очистить список
     * послать пакеты удаления объектов на клиент
     */
    suspend fun clear() {
        if (me is Player) {
            for (o in knownObjects.values) {
                me.session.send(ObjectDel(o))
            }
        }
        knownObjects.clear()
    }

    fun size(): Int {
        return knownObjects.size
    }
}
