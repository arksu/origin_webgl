package com.origin;

import com.origin.entity.Character;
import com.origin.entity.User;
import com.origin.jpa.MyEntityManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Database
{
	private static final Logger _log = LoggerFactory.getLogger(Database.class.getName());

	private static DataSource source;

	private static javax.persistence.EntityManager _em;

	private static MyEntityManager em2 = new MyEntityManager();

	public static void start()
	{
		User user2 = new User();
		user2.setLogin("12321");
		//**************************************************

		em2.addEntityClass(User.class);
		em2.addEntityClass(Character.class);

		em2.setConnectionFactory(Database::getConnection);

		//**************************************************
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("origin-app");
		_em = emf.createEntityManager();

		_em.getTransaction().begin();
		_em.persist(user2);
		_em.getTransaction().commit();

		User user = _em.find(User.class, 1);
		System.out.println(user.getId());

//		user.setLogin("some1222");
//
//		em.getTransaction().begin();
//		User user1 = em.merge(user);
//		System.out.println(user1);
//		em.getTransaction().commit();
//
//		em.flush();

		HikariConfig config = new HikariConfig();

		config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");

		config.setMinimumIdle(10);
		config.setMaximumPoolSize(20);

		config.addDataSourceProperty("user", ServerConfig.DB_USER);
		config.addDataSourceProperty("password", ServerConfig.DB_PASSWORD);
		config.addDataSourceProperty("databaseName", ServerConfig.DB_NAME);
		config.addDataSourceProperty("loginTimeout", 2);

		source = new HikariDataSource(config);

		try
		{
			Connection connection = getConnection();
			em2.deploy();
			connection.close();

//			try
//			{
//				connection = em2.beginTransaction();
			em2.persist(user2);
//			}
//			catch (SQLException e)
//			{
//			em2.rollback();
//			}

		}
		catch (SQLException e)
		{
			_log.error("connect error", e);
		}
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
