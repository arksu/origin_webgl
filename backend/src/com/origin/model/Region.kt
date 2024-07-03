package com.origin.model

import java.util.concurrent.ConcurrentHashMap

/**
 * игровая область (континент, материк)
 * в игре может быть несколько больших континентов одновременно
 */
class Region(val id: Int, val width: Int, val height: Int) {
    private val layers = ConcurrentHashMap<Int, LandLayer>()

    fun getLayer(level: Int): LandLayer {
        if (level < 0) throw RuntimeException("wrong grid level")
        return layers.computeIfAbsent(level) { LandLayer(this, level, width, height) }
    }
}
