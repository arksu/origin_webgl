package com.origin.model

import com.origin.entity.Character
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * основной статус живых объектов в игре (игрок, животные, NPC и прочее)
 * здоровье и стамина
 */
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
open class Status(val me: Human) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Status::class.java)

        /**
         * хелпер для действий: расход стамины
         */
        fun reduceStamina(value: Double): (Human) -> Boolean {
            return {
                it.status.checkAndReduceStamina(value)
            }
        }
    }

    var currentSoftHp: Double = 0.0
        protected set

    var currentStamina: Double = 0.0
        protected set

    var regenerationJob: Job? = null

    /**
     * снять хп (если опуститься до нуля - умереть)
     * @param isHPConsumption потребление хп? различные скиллы могут жрать хп. но умереть от этого нельзя
     * @return сколько хп было снято
     */
    open fun reduceSoftHp(value: Double, attacker: Human, isHPConsumption: Boolean): Double {
        val old = currentSoftHp
        if (value > 0) {
            setCurrentSoftHp(currentSoftHp - value)
        }

        if (!isHPConsumption && currentSoftHp < 0.5) {
            me.doDie()
        }
        return old - currentSoftHp
    }

    fun setCurrentSoftHp(value: Double): Boolean {
        if (me.isDead) return false

        val old = currentSoftHp
        val maxHp = me.getMaxSoftHp()

        currentSoftHp = if (value > maxHp) {
            maxHp
        } else if (value < 0) {
            0.0
        } else {
            value
        }
        val wasChanged = currentSoftHp != old
        if (wasChanged) {
            me.broadcastStatusUpdate()
        }
        logger.debug("hp $me $old -> $currentSoftHp")
        return wasChanged
    }

    fun checkAndReduceStamina(value: Double): Boolean {
        if (currentStamina >= value) {
            reduceStamina(value)
            return true
        }
        return false
    }

    /**
     * уменьшить стамину
     * @return на сколько фактически уменьшилась стамина
     */
    private fun reduceStamina(value: Double): Double {
        val old = currentStamina
        setCurrentStamina(currentStamina - value)
        return old - currentStamina
    }

    private fun setCurrentStamina(value: Double): Boolean {
        if (me.isDead) return false

        val old = currentStamina
        val maxStamina = me.getMaxStamina()

        currentStamina = if (value > maxStamina) {
            maxStamina
        } else if (value < 0) {
            0.0
        } else {
            value
        }
        val wasChanged = currentStamina != old
        if (wasChanged) {
            me.broadcastStatusUpdate()
        }
        return wasChanged
    }

    /**
     * заупстить восстановление хп, стамины, потребление энергии (голод)
     */
    fun startRegeneration() {
        if (regenerationJob != null) return

        val period = 3000L
        regenerationJob = WorkerScope.launch {
            while (true) {
                delay(period)
//                logger.debug("send StatusRegeneration")
                me.send(HumanMSg.StatusRegeneration())
            }
        }
    }

    fun stopRegeneration() {
        logger.debug("stopRegeneration")
        runBlocking {
            regenerationJob?.cancelAndJoin()
        }
        logger.debug("stopRegeneration after")
        regenerationJob = null
    }

    fun regeneration() {
        // TODO
        setCurrentStamina(currentStamina + 1.0)
    }

    fun TEST_restore() {
        setCurrentStamina(100.0)
    }

    open fun storeToCharacter(character: Character) {
        character.SHP = currentSoftHp
        character.stamina = currentStamina
    }
}
