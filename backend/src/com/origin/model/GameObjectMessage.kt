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

    /**
     * взять вещь из этого объекта
     */
    class TakeItem(val who: Human, val id: ObjectID) : MessageWithAck<InventoryItem?>()

    /**
     * положить вещь в этот объект
     */
    class PutItem(val item: InventoryItem, val x: Int, val y: Int) : MessageWithAck<Boolean>()

    // кто-то "открыл" объект
    class OpenBy(val who: Human)

    // кто-то "закрыл" объект
    class CloseBy(val who: Human)
}
