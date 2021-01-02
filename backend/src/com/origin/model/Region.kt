package com.origin.model

import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.concurrent.ConcurrentHashMap

/**
 * игровая область (континент, материк)
 * в игре может быть несколько больших континентов одновременно
 */
@ObsoleteCoroutinesApi
class Region(val id: Int) {
    private val layers = ConcurrentHashMap<Int, LandLayer>()

    fun getLayer(level: Int): LandLayer {
        if (level < 0) throw RuntimeException("wrong grid level")
        return layers.computeIfAbsent(level) { LandLayer(this, level) }
    }
}