package com.origin.model

import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi

@OptIn(ObsoleteCoroutinesApi::class)
class StaticObject(id: ObjectID, x: Int, y: Int, level: Int, region: Int, heading: Int) :
    GameObject(id, x, y, level, region, heading) {
}