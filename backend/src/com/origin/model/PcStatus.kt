package com.origin.model

import com.origin.entity.Character
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.math.max

/**
 * статус жизни, стамины, энергии игрока
 */
@ObsoleteCoroutinesApi
class PcStatus(me: Human, character: Character) : Status(me) {
    var currentHardHp: Double = character.HHP
        private set

    var currentEnergy: Double = character.energy
        private set

    init {
        currentSoftHp = character.SHP
        currentStamina = character.stamina
    }

    override fun reduceSoftHp(value: Double, attacker: Human, isHPConsumption: Boolean): Double {
        val old = currentSoftHp
        if (value > 0) {
            setCurrentSoftHp(max(currentSoftHp - value, 0.0))
        }

        if (currentSoftHp < 0.5) {
            me.doKnock()
        }
        return old - currentSoftHp
    }

    fun reduceHardHp(value: Double, attacker: Human, isHPConsumption: Boolean): Double {
        val old = currentHardHp
        if (value > 0) {
            setCurrentHardHp(max(currentHardHp - value, 0.0))
        }
        return old - currentHardHp
    }


    private fun setCurrentHardHp(value: Double): Boolean {
        if (me.isDead) return false

        val old = currentHardHp
        val maxHp = 100.0 // TODO maxHp

        if (value > maxHp) {
            currentHardHp = maxHp
        } else {
            currentHardHp = value
        }
        val wasChanged = currentHardHp != old
        if (wasChanged) {
            me.broadcastStatusUpdate()
        }
        logger.debug("hhp $me $old -> $currentHardHp")
        return wasChanged
    }
}
