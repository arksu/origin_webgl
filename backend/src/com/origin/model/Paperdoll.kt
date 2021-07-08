package com.origin.model

import com.origin.entity.InventoryItemEntity
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * кукла персонажа, что одето на персе
 */
@ObsoleteCoroutinesApi
class Paperdoll(val player: Player) {

    // TODO загрузка из базы

    /**
     * храним вещи одетые на сущность в массиве
     * доступ к элементам по индексу {@see Paperdoll.Slot}
     */
    private val items: Array<InventoryItemEntity>? = null

    enum class Slot(val id: Int) {
        LEFT_HAND(1), RIGHT_HAND(2), HEAD(3), EYES(4), BODY(5), LEGS(6);
    }
}
