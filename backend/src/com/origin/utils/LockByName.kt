package com.origin.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * Именованные блокировки
 */
class LockByName<T> {
    private val mapLock = ConcurrentHashMap<T, ReentrantLock>()

    fun getLock(key: T): ReentrantLock {
        val newLock = ReentrantLock()
        var lock = mapLock.putIfAbsent(key, newLock)
        if (lock == null) {
            lock = newLock
        }
        return lock
    }
}