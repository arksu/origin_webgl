package com.origin.model

import com.origin.ObjectID
import com.origin.util.ClientButton

sealed class PlayerMessage {
    class Connected
    class Disconnected
    class MapClick(val btn: ClientButton, val flags: Int, val x: Int, val y: Int)
    class ObjectClick(val id: ObjectID, val flags: Int, val x: Int, val y: Int)
    class ObjectRightClick(val id: ObjectID)

    // игрок кликнул по вещи в инвентаре
    class InventoryItemClick(val id: ObjectID, val inventoryId: ObjectID, val x: Int, val y: Int, val ox: Int, val oy: Int)
}