package com.origin

import com.origin.entity.Accounts
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val datasource = hikari()
        Database.connect(datasource)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(Accounts)
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