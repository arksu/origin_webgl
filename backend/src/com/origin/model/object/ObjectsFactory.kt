package com.origin.model.`object`

import com.origin.IdFactory
import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.GameObject
import com.origin.model.ObjectPosition

object ObjectsFactory {

    private val map = HashMap<Int, Class<GameObject>>()
    private val reverseMap = HashMap<Class<GameObject>, Int>()

    fun add(typeId: Int, clazz: Class<*>) {
        @Suppress("UNCHECKED_CAST")
        map[typeId] = clazz as Class<GameObject>
        reverseMap[clazz] = typeId
    }

    fun create(record: ObjectRecord): GameObject {
        val clazz = map[record.type] ?: throw RuntimeException("class game object for type ${record.type} not found")
        val c = clazz.getConstructor(ObjectRecord::class.java)
        val gameObject = c.newInstance(record)
        gameObject.postConstruct()
        return gameObject
    }

    fun create(clazz: Class<GameObject>, pos: ObjectPosition, data: String? = null): GameObject {
        val typeId = reverseMap[clazz] ?: throw RuntimeException("type id for $clazz game object not found")

        val record = ObjectRecord(
            id = IdFactory.getNext(),
            region = pos.region,
            x = pos.x,
            y = pos.y,
            level = pos.level,
            heading = pos.heading,
            gridX = pos.gridX,
            gridY = pos.gridY,
            type = typeId,
            quality = 10,
            hp = 100,
            createTick = 0,
            lastTick = 0,
            data = data
        )

        val c = clazz.getConstructor(ObjectRecord::class.java)
        val gameObject = c.newInstance(record)
        gameObject.postConstruct()
        return gameObject
    }

    fun init() {
        val packageName = "com.origin.model.object"
        val reflections = org.reflections.Reflections(packageName)
        val objectClasses = reflections.getSubTypesOf(GameObject::class.java)
        for (clazz in objectClasses) {
            Class.forName(clazz.name)
        }
    }
}