package com.origin.model.message

import com.origin.ObjectID
import com.origin.net.GameSocket
import com.origin.util.ClientButton
import com.origin.util.MessageWithAck

sealed class PlayerMessage {
    class Connected
    class Disconnected
    class Attach(val socket: GameSocket) : MessageWithAck<Boolean>()
    class KeyDown(val key: String)
    class MapClick(val btn: ClientButton, val flags: Int, val x: Int, val y: Int)
    class ObjectClick(val id: ObjectID, val flags: Int, val x: Int, val y: Int)
    class ObjectRightClick(val id: ObjectID)
    class InventoryClose(val id: ObjectID)
    class ChatMessage(val text: String)
    class ContextMenuItem(val item: String)

    // игрок кликнул по вещи в инвентаре
    class InventoryItemClick(val id: ObjectID, val inventoryId: ObjectID, val x: Int, val y: Int, val ox: Int, val oy: Int)

    class InventoryRightItemClick(val id: ObjectID, val inventoryId: ObjectID)

    class Craft(val name: String)
    class Action(val name: String)
}