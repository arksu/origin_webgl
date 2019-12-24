package com.origin.jpa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MyEntityManager
{
	private Map<Class, ClassDescriptor> _descriptors = new HashMap<>(4);

	private ConnectionFactory _connectionFactory;

	/**
	 * фабрика для получения коннектов к базе
	 */
	public interface ConnectionFactory
	{
		Connection get();
	}

	/**
	 * передадим фабрику коннектов
	 */
	public void setConnectionFactory(ConnectionFactory factory)
	{
		_connectionFactory = factory;
	}

	public Connection beginTransaction() throws SQLException
	{
		Connection connection = _connectionFactory.get();
		connection.setAutoCommit(false);
		return connection;
	}

	public void commit(Connection connection) throws SQLException
	{
		connection.setAutoCommit(true);
		connection.commit();
		connection.close();
	}

	public void rollback(Connection connection) throws SQLException
	{
		connection.rollback();
		connection.setAutoCommit(true);
		connection.close();
	}

	/**
	 * добавить класс сущности
	 */
	public void addEntityClass(Class<?> clazz)
	{
		ClassDescriptor descriptor = new ClassDescriptor(clazz);
		_descriptors.put(clazz, descriptor);
	}

	/**
	 * деплой сущностей в базу если необходимо
	 */
	public void deploy() throws SQLException
	{
		for (ClassDescriptor descriptor : _descriptors.values())
		{
			descriptor.deploy(_connectionFactory.get());
		}
	}

	public void persist(Object entity)
	{
		persist(entity, _connectionFactory.get());
	}

	public void persist(Object entity, Connection connection)
	{
		ClassDescriptor descriptor = getDescriptor(entity);
		if (descriptor == null)
		{
			throw new IllegalArgumentException("Not entity object, no class descriptor");
		}

		// проходим по всем полям дескриптора
		for (DatabaseField field : descriptor.getFields())
		{
		}
	}

	public <T> T find(Class<T> entityClass, Object primaryKey)
	{
		return null;
	}

	public ClassDescriptor getDescriptor(Object entity)
	{
		if (entity == null)
		{
			return null;
		}
		return _descriptors.get(entity.getClass());
	}
}
