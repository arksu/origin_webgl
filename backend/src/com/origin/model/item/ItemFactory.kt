package com.origin.model.item

import com.origin.IdFactory
import com.origin.jooq.tables.records.InventoryRecord
import com.origin.model.item.food.Apple

object ItemFactory {
    val map = HashMap<Int, Class<Item>>()
    val mapNames = HashMap<String, Int>()

    fun init() {
        Apple.Companion
        StoneAxe.Companion
        Stone.Companion
        Branch.Companion
        Rabbit.Companion
        Board.Companion
        Bark.Companion
        Bucket.Companion
    }

    fun add(typeId: Int, clazz: Class<Item>) {
        map[typeId] = clazz
        mapNames[clazz.simpleName.lowercase()] = typeId
    }

    fun create(record: InventoryRecord): Item {
        val clazz = map[record.type] ?: throw RuntimeException("class item for type ${record.type} not found")
        val c = clazz.getConstructor(InventoryRecord::class.java)
        return c.newInstance(record)
    }

    fun create(typeId: Int, count: Int = 1, quality: Short = 10): Item {
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
        return create(record)
    }

    fun getTypeByName(typeName: String): Int {
        return mapNames[typeName] ?: 0
    }
}