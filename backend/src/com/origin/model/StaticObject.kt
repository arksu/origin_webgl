package com.origin.model

import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class StaticObject(id: ObjectID, x: Int, y: Int, level: Int, region: Int, heading: Short) :
    GameObject(id, x, y, level, region, heading) {
}