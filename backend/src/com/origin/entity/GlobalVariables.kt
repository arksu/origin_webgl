package com.origin.entity

import com.origin.upsert
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * глобальные переменные сервера которые храним в базе
 */
object GlobalVariables : Table("globalVariables") {
    val name = varchar("name", 32)
    val valueLong = long("valueLong").nullable()
    val valueString = varchar("valueString", 1024).nullable()

    override val primaryKey by lazy { PrimaryKey(name) }

    fun getLong(name: String, default: Long = 0): Long {
        val row = transaction {
            GlobalVariables.select { GlobalVariables.name eq name }.singleOrNull()
        } ?: return default;
        return row[valueLong] ?: throw RuntimeException("no value")
    }

    fun getString(name: String, default: String): String {
        val row = transaction {
            GlobalVariables.select { GlobalVariables.name eq name }.singleOrNull()
        } ?: return default
        return row[valueString] ?: throw RuntimeException("no value")
    }

    fun saveLong(name: String, value: Long) {
        transaction {
            GlobalVariables.upsert(GlobalVariables.name) {
                it[GlobalVariables.name] = name
                it[valueLong] = value
            }
        }
    }

    fun saveString(name: String, value: String) {
        transaction {
            GlobalVariables.upsert(GlobalVariables.name) {
                it[GlobalVariables.name] = name
                it[valueString] = value
            }
        }
    }
}