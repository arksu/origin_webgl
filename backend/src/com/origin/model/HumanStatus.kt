package com.origin.model

import com.origin.net.CUR_STAMINA
import com.origin.net.MAX_STAMINA
import com.origin.net.StatusUpdate

open class HumanStatus(private val me: Human) {
    @Volatile
    var stamina: Int = 0
        protected set

    open val maxStamina: Int = 1000

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

    open fun fillStatusUpdate(su: StatusUpdate) {
        su.addAttribute(CUR_STAMINA, stamina)
        su.addAttribute(MAX_STAMINA, maxStamina)
    }

    open fun diffStatusUpdate(old: StatusUpdate, su: StatusUpdate) {
        // TODO
    }

    //    var lastStatusUpdate: StatusUpdate? = null
    fun getPacket(): StatusUpdate {
        val pkt = StatusUpdate(me)
        fillStatusUpdate(pkt)

//        val pkt = if (lastStatusUpdate == null) {
//            val su = StatusUpdate(player)
//            fillStatusUpdate(su)
//            su
//        } else {
//            // TODO diff lastStatusUpdate <-> su
//        }
//        lastStatusUpdate = pkt
        return pkt
    }

}