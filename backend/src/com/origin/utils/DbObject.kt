package com.origin.utils

import com.origin.Database

/**
 * небольшая обертка для удобства вызова persist у экземпляра сущности
 */
open class DbObject {
    fun persist() {
        Database.em().persist(this)
    }
}