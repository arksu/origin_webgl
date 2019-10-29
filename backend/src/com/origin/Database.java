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
