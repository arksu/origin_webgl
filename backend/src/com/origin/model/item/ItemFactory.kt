package com.origin.model.item

import com.origin.IdFactory
import com.origin.jooq.tables.records.InventoryRecord

object ItemFactory {

    private val map = HashMap<Int, Class<Item>>()
    private val mapNames = HashMap<String, Int>()

    fun add(typeId: Int, clazz: Class<*>) {
        @Suppress("UNCHECKED_CAST")
        map[typeId] = clazz as Class<Item>
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

    fun init() {
        val packageName = "com.origin.model.item"
        val reflections = org.reflections.Reflections(packageName)
        val objectClasses = reflections.getSubTypesOf(Item::class.java)
        for (clazz in objectClasses) {
            Class.forName(clazz.name)
        }
    }
}