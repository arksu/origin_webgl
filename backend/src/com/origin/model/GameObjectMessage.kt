package com.origin.model

import com.origin.util.MessageWithAck

sealed class GameObjectMessage {
    class Spawn(val variants: List<SpawnType>) : MessageWithAck<Boolean>()

    /**
     * грид добавил себе какой-то объект и нас уведомил об этом
     */
    class GridObjectAdded(obj: GameObject)

    /**
     * грид удалил у себя какой-то объект
     */
    class GridObjectRemoved(obj: GameObject)

    /**
     * этот объект был удален из грида
     */
    class RemovedFromGrid()
}
