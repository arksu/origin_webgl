package com.origin.model.message

import com.origin.model.`object`.GameObject
import com.origin.model.SpawnType
import com.origin.util.MessageWithAck

sealed class GameObjectMessage {
    class Spawn(val variants: List<SpawnType>) : MessageWithAck<Boolean>()

    /**
     * грид добавил себе какой-то объект и нас уведомил об этом
     */
    class GridObjectAdded(val obj: GameObject)

    /**
     * грид удалил у себя какой-то объект
     */
    class GridObjectRemoved(val obj: GameObject)

    /**
     * этот объект был удален из грида
     */
    class RemovedFromGrid
}
