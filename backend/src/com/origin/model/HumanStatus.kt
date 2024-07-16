package com.origin.model

open class HumanStatus(private val me: Human) {
    @Volatile
    var stamina: Int = 0
        protected set

    fun checkAndReduceStamina(value: Int): Boolean {
        if (me.isDead) return false

        if (stamina >= value) {
            reduceStamina(value)
            return true
        }
        return false
    }

    /**
     * уменьшить стамину
     * @return на сколько фактически уменьшилась стамина
     */
    private fun reduceStamina(value: Int): Int {
        val old = stamina
        setCurrentStamina(stamina - value)
        return old - stamina
    }

    private fun setCurrentStamina(value: Int): Boolean {

        val old = stamina
        val maxStamina = me.getMaxStamina()

        stamina = if (value > maxStamina) {
            maxStamina
        } else if (value < 0) {
            0
        } else {
            value
        }
        val wasChanged = stamina != old
        if (wasChanged) {
            me.broadcastStatusUpdate()
        }
        return wasChanged
    }

}