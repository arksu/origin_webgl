package com.origin.model

import com.origin.entity.EntityPosition

/**
 * гуманоид
 * обладает зрением (видимые объекты о которых "знает")
 */
open class Human(pos: EntityPosition) : MovingObject(pos) {

    /**
     * добавили объект в грид в котором находится объект
     */
    fun onObjectAdded(obj: GameObject) {
    }

    /**
     * грид говорит что какой то объект был удален
     */
    fun onObjectRemoved(obj: GameObject) {
    }

}