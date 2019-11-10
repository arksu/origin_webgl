package com.origin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Database
{
	private static final Logger _log = LoggerFactory.getLogger(Database.class.getName());

	private static DataSource source;

//	private static EntityManager _em;

	public static void start()
	{
//		EntityManagerFactory emf = Persistence.createEntityManagerFactory("origin-app");
//		_em = emf.createEntityManager();
//
//		_em.getCriteriaBuilder()

//		User user = em.find(User.class, 1);
//		System.out.println(user.getId());
//
//		user.setLogin("some1222");
//
//		em.getTransaction().begin();
//		User user1 = em.merge(user);
//		System.out.println(user1);
//		em.getTransaction().commit();
//
//		em.flush();

//		HikariConfig config = new HikariConfig();

//		config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
//
//		config.setMinimumIdle(10);
//		config.setMaximumPoolSize(20);
//
//		config.addDataSourceProperty("user", ServerConfig.DB_USER);
//		config.addDataSourceProperty("password", ServerConfig.DB_PASSWORD);
//		config.addDataSourceProperty("databaseName", ServerConfig.DB_NAME);
//		config.addDataSourceProperty("loginTimeout", 2);
//
//		source = new HikariDataSource(config);

//		try
//		{
//			getConnection().close();
//		}
//		catch (SQLException e)
//		{
//			_log.error("connect close error", e);
//		}
	}

//	public static EntityManager getEM()
//	{
//		return _em;
//	}

	/**
	 * получить коннект до базы
	 */
	public static Connection getConnection()
	{
		try
		{
			return source.getConnection();
		}
		catch (SQLException e)
		{
			_log.error("get connection error: " + e.getMessage(), e);
		}
		throw new RuntimeException("no available connection");
	}
}
