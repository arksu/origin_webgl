package com.origin.model

import com.origin.entity.EntityPosition
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi

@OptIn(ObsoleteCoroutinesApi::class)
class StaticObject(id: ObjectID, entityPosition: EntityPosition) : GameObject(id, entityPosition) {
}