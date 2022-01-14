package com.origin.model.objects.containers

import com.origin.entity.EntityObject
import com.origin.model.*
import com.origin.model.inventory.Inventory
import com.origin.utils.ObjectID
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * контейнеры которые могут хранить вещи (ящики, шкафы и тд)
 */
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
abstract class Container(entity: EntityObject) : StaticObject(entity) {

    /**
     * создаем (а значит и загрузим) инвентарь при первом обращени к нему, этого момента его в объекте не будет
     * и из базы он загружен не будет
     */
    override val inventory by lazy { Inventory(this) }

    /**
     * список тех кто открыл данный контейнер
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
            is GameObjectMsg.PutItem -> {
                msg.resp.complete(inventory.putItem(msg.item, msg.x, msg.y))
            }
            is GameObjectMsg.TakeItem -> {
                msg.resp.complete(inventory.takeItem(msg.id))
            }
            else -> super.processMessage(msg)
        }
    }

    override fun getResourcePath(): String {
        // если есть хоть кто-то в "открывших" инвентарь
        return if (discoverers.size > 0) openResource else normalResource
    }

    override suspend fun openBy(who: Human) {
        super.openBy(who)

        val oldSize = discoverers.size
        discoverers[who.id] = who
        // если это первый открывший - надо всем отослать эвент изменения состояния
        // это заставит получить новое имя ресурса и отправить его клиентам
        if (oldSize == 0) {
            grid.broadcast(BroadcastEvent.Changed(this))
        }
    }

    override suspend fun closeBy(who: Human) {
        super.closeBy(who)

        discoverers.remove(who.id)
        // если после закрытия не осталось тех кто открывает контейнер
        // надо послать эвент об изменении (закрытии)
        if (discoverers.size == 0) {
            grid.broadcast(BroadcastEvent.Changed(this))
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
