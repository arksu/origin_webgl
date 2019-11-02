package com.origin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public class Database
{
	public static void start()
	{
		HikariConfig config = new HikariConfig();

		config.setJdbcUrl("");
		config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		config.setUsername(ServerConfig.DB_USER);
		config.setPassword(ServerConfig.DB_PASSWORD);
		config.addDataSourceProperty("cachePrepStmts", true);
		config.addDataSourceProperty("prepStmtCacheSize", 250);
		config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);

		DataSource source = new HikariDataSource(config);

		try
		{
			source.getConnection().close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
