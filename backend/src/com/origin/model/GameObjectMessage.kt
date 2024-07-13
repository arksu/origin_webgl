package com.origin.model

import com.origin.ObjectID
import com.origin.model.inventory.InventoryItem
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
