package com.origin;

import com.origin.entity.Character;
import com.origin.entity.User;
import com.origin.jpa.EntityManager;
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

	private static javax.persistence.EntityManager _em1;

	private static EntityManager _em = new EntityManager();

	public static void start()
	{
		_em.addEntityClass(User.class);
		_em.addEntityClass(Character.class);

		_em.setConnectionFactory(Database::getConnection);
		//**************************************************

		// TEST code
		User user2 = new User();
		user2.setLogin("test_login1");

		//**************************************************
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("origin-app");
		_em1 = emf.createEntityManager();

//		_em.getTransaction().begin();
//		_em.persist(user2);
//		_em.getTransaction().commit();

//		User user3 = _em1.find(User.class, 1);
//		_em1.getTransaction().begin();
//		user3.setLogin("user33");
//		_em1.getTransaction().commit();

//		User user = _em.find(User.class, 1);
//		System.out.println(user.getId());

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
			_em.deploy();
			connection.close();

			_em.persist(user2);

			final User user = _em.findById(User.class, 1);
			_log.debug(user.getLogin());

			_em.refresh(user);
			_log.debug(user.getLogin());

			_em.remove(user);

			_em.persist(user);

			user2.setLogin("updatedLogin");
			user2.setPassword("updPassword");
			_em.persist(user2);
		}
		catch (SQLException e)
		{
			_log.error("connect error", e);
		}
	}

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
