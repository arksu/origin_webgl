package com.origin.model

import com.origin.entity.InventoryItem

/**
 * кукла персонажа, что одето на персе
 */
class Paperdoll(val player: Player) {

    /**
     * храним вещи одетые на сущность в массиве
     * доступ к элементам по индексу {@see Paperdoll.Slot}
     */
    private val items: Array<InventoryItem>? = null;

    enum class Slot(val id: Int) {
        LEFT_HAND(1), RIGHT_HAND(2), HEAD(3), EYES(4), BODY(5), LEGS(6);

    }
}