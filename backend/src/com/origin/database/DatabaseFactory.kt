package com.origin.database

import com.origin.ServerConfig
import com.origin.entity.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat

class TimestampColumnType(private val defaultCurrentTimestamp: Boolean, private val onUpdateCurrentTimestamp: Boolean) :
    ColumnType() {
    override fun sqlType(): String =
        "TIMESTAMP ${if (defaultCurrentTimestamp) "DEFAULT CURRENT_TIMESTAMP" else ""} ${if (onUpdateCurrentTimestamp) "ON UPDATE CURRENT_TIMESTAMP" else ""}"

    override fun valueFromDB(value: Any): Timestamp = when (value) {
        is Timestamp -> value
        else -> error("Unexpected value of type Byte: $value of ${value::class.qualifiedName}")
    }

    override fun notNullValueToDB(value: Any): Any {
        return if (value is Timestamp) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value)
        } else {
            "0"
        }
    }
}

fun Table.timestamp(
    name: String,
    defaultCurrentTimestamp: Boolean = false,
    onUpdateCurrentTimestamp: Boolean = false,
): Column<Timestamp> =
    registerColumn(name, TimestampColumnType(defaultCurrentTimestamp, onUpdateCurrentTimestamp))


object DatabaseFactory {
    val logger: Logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

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
                GlobalVariables,
                ChatHistory
            )
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mariadb://${ServerConfig.DB_HOST}:${ServerConfig.DB_PORT}/${ServerConfig.DB_NAME}"

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