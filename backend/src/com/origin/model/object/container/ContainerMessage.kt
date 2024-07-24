package com.origin.model.`object`.container

import com.origin.ObjectID
import com.origin.model.Human
import com.origin.model.item.Item
import com.origin.util.MessageWithAck

sealed class ContainerMessage {

    /**
     * кто-то "открыл" объект
     */
    class OpenBy(val who: Human)

    /**
     * кто-то "закрыл" объект
     */
    class CloseBy(val who: Human)

    /**
     * положить вещь в этот объект
     */
    class PutItem(val item: Item, val x: Int, val y: Int) : MessageWithAck<Boolean>()

    /**
     * взять вещь из этого объекта
     */
    class TakeItem(val who: Human, val id: ObjectID) : MessageWithAck<Item?>()

}