package com.origin.model

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.max

/**
 * основной статус живых объектов в игре (игрок, животные, NPC и прочее)
 * здоровье и стамина
 */
@ObsoleteCoroutinesApi
open class Status(val me: Human) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Status::class.java)
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
            setCurrentSoftHp(max(currentSoftHp - value, 0.0))
        }

        if (currentSoftHp < 0.5) {
            me.doDie()
        }
        return old - currentSoftHp
    }

    fun setCurrentSoftHp(value: Double): Boolean {
        if (me.isDead) return false

        val old = currentSoftHp
        val maxHp = me.getMaxSoftHp()

        if (value > maxHp) {
            currentSoftHp = maxHp
        } else {
            currentSoftHp = value
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

    private fun reduceStamina(value: Double): Double {
        val old = currentStamina
        setCurrentStamina(max(currentStamina - value, 0.0))
        return old - currentStamina
    }

    private fun setCurrentStamina(value: Double): Boolean {
        if (me.isDead) return false

        val old = currentStamina
        val maxStamina = me.getMaxStamina()

        if (value > maxStamina) {
            currentStamina = maxStamina
        } else {
            currentStamina = value
        }
        val wasChanged = currentStamina != old
        if (wasChanged) {
            me.broadcastStatusUpdate()
        }
        logger.debug("stamina $me $old -> $currentStamina")
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
//                me.send(HumanMSg.StatusRegeneration())
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
}