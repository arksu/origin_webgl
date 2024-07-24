package com.origin.model.`object`

import com.origin.IdFactory
import com.origin.jooq.tables.records.InventoryRecord
import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.GameObject
import com.origin.model.ObjectPosition
import com.origin.model.item.Item
import com.origin.model.`object`.container.Box
import com.origin.model.`object`.container.Crate
import com.origin.model.`object`.tree.*

object ObjectsFactory {
    fun constructByRecord(record: ObjectRecord): GameObject {
        val obj = when (record.type) {
            1 -> Box(record)
            2 -> Birch(record)
            3 -> Fir(record)
            4 -> Pine(record)
            5 -> Apple(record)
            6 -> Oak(record)
            7 -> Elm(record)
            8 -> Hazel(record)
            9 -> Maple(record)
            10 -> Willow(record)
            11 -> Yew(record)
            12 -> Crate(record)
            13 -> Stone(record)
            14 -> WoodenLog(record)
            else -> UnknownObject(record)
        }
        obj.afterLoad()
        return obj
    }

    fun create(type: Int, pos: ObjectPosition, data : String? = null): ObjectRecord {
        return ObjectRecord(
            id = IdFactory.getNext(),
            region = pos.region,
            x = pos.x,
            y = pos.y,
            level = pos.level,
            heading = pos.heading,
            gridX = pos.gridX,
            gridY = pos.gridY,
            type = type,
            quality = 10,
            hp = 100,
            createTick = 0,
            lastTick = 0,
            data = data
        )
    }

    fun createInventoryItem(typeId: Int, count: Int = 1, quality: Short = 10): Item {
        val record = InventoryRecord(
            id = IdFactory.getNext(),
            inventoryId = -1, // укажем -1 значит попытка спавна
            x = 0,
            y = 0,
            type = typeId,
            quality = quality,
            count = count,
            lastTick = 0,
            deleted = 0
        )
        return Item(record)
    }
}