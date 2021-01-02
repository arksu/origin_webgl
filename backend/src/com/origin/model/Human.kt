package com.origin.model

import com.origin.entity.EntityPosition
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * гуманоид
 * обладает зрением (видимые объекты о которых "знает")
 */
@ObsoleteCoroutinesApi
open class Human(id: ObjectID, pos: EntityPosition) : MovingObject(id, pos) {

    /**
     * добавили объект в грид в котором находится объект
     */
    override fun onObjectAdded(obj: GameObject) {
    }

    /**
     * грид говорит что какой то объект был удален
     */
    override fun onObjectRemoved(obj: GameObject) {
    }

}