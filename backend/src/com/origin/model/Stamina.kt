package com.origin.model

import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * стамина, выносливость персонажа (энергия расходуемая на различные действия)
 */
@ObsoleteCoroutinesApi
class Stamina(val me: Player) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Stamina::class.java)
    }

    var current: Int = 100
    var max: Int = 100

    /**
     * взять и израсходовать часть стамины
     * @return true если хватает
     */
    fun take(value: Int): Boolean {
        if (value <= current) {
            val old = current
            current -= value
            logger.debug("take stamina $old -> $current")
            return true
        }
        logger.warn("no enough stamina cur=$current req=$value")
        return false
    }

    /**
     * восстановить часть стамины
     */
    fun restore(value: Int) {
        current += value
        if (current > max) {
            current = max
        }
    }
}