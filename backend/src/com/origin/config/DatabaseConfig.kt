package com.origin.config

import com.origin.ServerConfig
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager

object DatabaseConfig {

    val dsl by lazy {
        val connection: Connection = DriverManager.getConnection(ServerConfig.DATABASE_URL, ServerConfig.DATABASE_USER, ServerConfig.DATABASE_PASSWORD)
        DSL.using(connection, org.jooq.SQLDialect.MARIADB)
    }
}