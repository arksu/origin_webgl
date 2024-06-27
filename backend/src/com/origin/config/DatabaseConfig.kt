package com.origin.config

import com.origin.ServerConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.impl.DSL

object DatabaseConfig {

    val dsl by lazy {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = ServerConfig.DATABASE_URL
            username = ServerConfig.DATABASE_USER
            password = ServerConfig.DATABASE_USER
            driverClassName = "org.mariadb.jdbc.Driver"
            maximumPoolSize = ServerConfig.DATABASE_MAX_POOL_SIZE
        }
        val dataSource = HikariDataSource(hikariConfig)

        DSL.using(dataSource, org.jooq.SQLDialect.MARIADB)
    }
}