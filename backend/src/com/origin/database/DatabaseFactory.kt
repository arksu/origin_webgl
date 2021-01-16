package com.origin

import com.origin.entity.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Timestamp

class TimestampColumnType(private val defaultCurrentTimestamp: Boolean) : ColumnType() {
    override fun sqlType(): String = "TIMESTAMP ${if (defaultCurrentTimestamp) "DEFAULT CURRENT_TIMESTAMP" else ""}"

    override fun valueFromDB(value: Any): Timestamp = when (value) {
        is Timestamp -> value
        else -> error("Unexpected value of type Byte: $value of ${value::class.qualifiedName}")
    }
}

fun Table.timestamp(name: String, defaultCurrentTimestamp: Boolean = false): Column<Timestamp> =
    registerColumn(name, TimestampColumnType(defaultCurrentTimestamp))


object DatabaseFactory {

    fun init() {
        logger.info("Database init...")
        val datasource = hikari()
        Database.connect(datasource)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Accounts,
                Grids,
                Characters,
                EntityObjects,
                InventoryItems,
                GlobalVariables
            )
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mariadb://${ServerConfig.DB_HOST}/${ServerConfig.DB_NAME}"

            addDataSourceProperty("user", ServerConfig.DB_USER)
            addDataSourceProperty("password", ServerConfig.DB_PASSWORD);
            addDataSourceProperty("loginTimeout", 2);
            addDataSourceProperty("autoReconnect", true);

            isAutoCommit = false

            minimumIdle = 10
            maximumPoolSize = 20
            leakDetectionThreshold = 5000
            connectionTimeout = 30000

        }
        return HikariDataSource(config)
    }
}