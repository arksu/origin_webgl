package com.origin.model.`object`.container

import com.origin.ObjectID
import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.BroadcastEvent
import com.origin.model.Human
import com.origin.model.Player
import com.origin.model.StaticObject
import com.origin.model.inventory.Inventory

/**
 * контейнеры, которые могут хранить вещи (ящики, шкафы и тд)
 */
abstract class Container(record: ObjectRecord) : StaticObject(record) {
    /**
     * создаем (а значит и загрузим) инвентарь при первом обращении к нему, этого момента его в объекте не будет
     * и из базы он загружен не будет
     */
    override val inventory by lazy { Inventory(this) }

    /**
     * список тех кто открыл данный контейнер
     * объекты которые "держат" меня в открытом состоянии,
     * взаимодействуют со мной
     */
    private val discoverers = HashMap<ObjectID, Human>()

    /**
     * имя ресурса объекта в нормальном состоянии
     */
    abstract val normalResource: String

    /**
     * как показываем "открытое" состояние
     */
    abstract val openResource: String

    override suspend fun processMessage(msg: Any) {
        when (msg) {
            is ContainerMessage.OpenBy -> onOpenBy(msg.who)
            is ContainerMessage.CloseBy -> onCloseBy(msg.who)
            is ContainerMessage.PutItem -> msg.run { inventory.putItem(msg.item, msg.x, msg.y) }
            is ContainerMessage.TakeItem -> msg.run { inventory.takeItem(msg.id) }
            else -> super.processMessage(msg)
        }
    }

    override fun getResourcePath(): String {
        // если есть хоть кто-то в "открывших" инвентарь
        return if (discoverers.size > 0) openResource else normalResource
    }

    private suspend fun onOpenBy(who: Human) {
        val oldSize = discoverers.size
        discoverers[who.id] = who
        // если это первый открывший - надо всем отослать эвент изменения состояния
        // это заставит получить новое имя ресурса и отправить его клиентам
        if (oldSize == 0) {
            getGridSafety().broadcast(BroadcastEvent.Changed(this))
        }
    }

    private suspend fun onCloseBy(who: Human) {
        discoverers.remove(who.id)
        // если после закрытия не осталось тех кто открывает контейнер
        // надо послать эвент об изменении (закрытии)
        if (discoverers.size == 0) {
            getGridSafety().broadcast(BroadcastEvent.Changed(this))
        }
    }

    suspend fun inventoryChanged() {
        discoverers.forEach {
            val p = it.value
            if (p is Player) {
                inventory.send(p)
            }
        }
    }
}