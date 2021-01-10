package com.origin.model

import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * гуманоид
 * обладает зрением (видимые объекты о которых "знает")
 */
@ObsoleteCoroutinesApi
abstract class Human(id: ObjectID, x: Int, y: Int, level: Int, region: Int, heading: Int) :
    MovingObject(id, x, y, level, region, heading) {

    /**
     * дистанция на которой мы видим объекты
     * может изменяться динамически (ночью видим хуже)
     */
    private var visibleDistance = 500

    /**
     * объекты которые известны мне, инфа о которых отправляется и синхронизирована с клиентом
     * любое добавление в этот список, а равно как и удаление из него должно быть
     * синхронизировано с клиентом
     */
    private val knownList by lazy { KnownList(this) }

    /**
     * добавили объект в грид в котором находится объект
     */
    override suspend fun onObjectAdded(obj: GameObject) {
        if (isObjectVisibleForMe(obj)) {
            knownList.addKnownObject(obj)
        }
    }

    /**
     * грид говорит что какой то объект был удален
     */
    override suspend fun onObjectRemoved(obj: GameObject) {
        logger.debug("human onObjectRemoved $obj")
        knownList.removeKnownObject(obj)
    }

    /**
     * вижу ли я указаный объект
     */
    private fun isObjectVisibleForMe(obj: GameObject): Boolean {
        // себя всегда видим!
        return obj.id == this.id || pos.getDistance(obj.pos) < visibleDistance
    }

    /**
     * обновить список видимых объектов
     * все новые что увидим - отправятся клиенту. старые что перестали видеть - будут удалены
     * @param force принудительно, иначе проверка будет только если отошли на значительное расстояние от точки последней проверки
     */
    fun updateVisibleObjects(force: Boolean) {
        // TODO updateVisibleObjects
    }

}