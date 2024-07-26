package com.origin.model.item

import com.origin.IdFactory
import com.origin.jooq.tables.records.InventoryRecord

object ItemFactory {

    private val map = HashMap<Int, Class<Item>>()
    private val reverseMap = HashMap<Class<Item>, Int>()
    private val mapNames = HashMap<String, Class<Item>>()

    fun add(typeId: Int, clazz: Class<*>) {
        @Suppress("UNCHECKED_CAST")
        map[typeId] = clazz as Class<Item>
        reverseMap[clazz] = typeId
        mapNames[clazz.simpleName.lowercase()] = clazz
    }

    fun create(record: InventoryRecord): Item {
        val clazz = map[record.type] ?: throw RuntimeException("item class for type ${record.type} not found")
        val c = clazz.getConstructor(InventoryRecord::class.java)
        return c.newInstance(record)
    }

    fun create(clazz: Class<*>, count: Int = 1, quality: Short = 10): Item {
        val typeId = reverseMap[clazz] ?: throw RuntimeException("item type for class ${clazz.simpleName} not found")
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

    fun getTypeByClass(clazz: Class<*>): Int {
        return reverseMap[clazz] ?: throw RuntimeException("item type for class ${clazz.simpleName} not found")
    }

    fun getClassByName(typeName: String): Class<Item>? {
        return mapNames[typeName.lowercase()]
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