package com.origin.model

import com.origin.entity.EntityPosition
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * гуманоид
 * обладает зрением (видимые объекты о которых "знает")
 */
@ObsoleteCoroutinesApi
open class Human(pos: EntityPosition) : MovingObject(pos) {

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