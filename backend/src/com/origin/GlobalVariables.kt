package com.origin

import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.references.GLOBAL_VAR

object GlobalVariables {
    fun getLong(name: String, default: Long = 0): Long {
        val row = DatabaseConfig.dsl
            .selectFrom(GLOBAL_VAR)
            .where(GLOBAL_VAR.NAME.eq(name))
            .fetchOne()
        return row?.valueLong ?: default
    }

    fun getString(name: String, default: String): String {
        val row = DatabaseConfig.dsl
            .selectFrom(GLOBAL_VAR)
            .where(GLOBAL_VAR.NAME.eq(name))
            .fetchOne()
        return row?.valueString ?: default
    }

    fun saveLong(name: String, value: Long) {
        DatabaseConfig.dsl
            .insertInto(GLOBAL_VAR)
            .set(GLOBAL_VAR.NAME, name)
            .set(GLOBAL_VAR.VALUE_LONG, value)
            .onConflict(GLOBAL_VAR.NAME.eq(name))
            .doUpdate()
            .set(GLOBAL_VAR.VALUE_LONG, value)
            .execute()
    }

    fun saveString(name: String, value: String) {
        DatabaseConfig.dsl
            .insertInto(GLOBAL_VAR)
            .set(GLOBAL_VAR.NAME, name)
            .set(GLOBAL_VAR.VALUE_STRING, value)
            .onConflict(GLOBAL_VAR.NAME.eq(name))
            .doUpdate()
            .set(GLOBAL_VAR.VALUE_STRING, value)
            .execute()
    }
}