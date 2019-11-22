package com.origin.jpa;

import javax.persistence.EntityExistsException;
import javax.persistence.TransactionRequiredException;
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

	/**
	 * Make an instance managed and persistent.
	 * @param entity entity instance
	 * @throws EntityExistsException        if the entity already exists.
	 *                                      (If the entity already exists, the <code>EntityExistsException</code> may
	 *                                      be thrown when the persist operation is invoked, or the
	 *                                      <code>EntityExistsException</code> or another <code>PersistenceException</code> may be
	 *                                      thrown at flush or commit time.)
	 * @throws IllegalArgumentException     if the instance is not an
	 *                                      entity
	 * @throws TransactionRequiredException if there is no transaction when
	 *                                      invoked on a container-managed entity manager of that is of type
	 *                                      <code>PersistenceContextType.TRANSACTION</code>
	 */
	public void persist(Object entity, Connection connection)
	{

	}

	/**
	 * Merge the state of the given entity into the
	 * current persistence context.
	 * @param entity entity instance
	 * @return the managed instance that the state was merged to
	 * @throws IllegalArgumentException     if instance is not an
	 *                                      entity or is a removed entity
	 * @throws TransactionRequiredException if there is no transaction when
	 *                                      invoked on a container-managed entity manager of that is of type
	 *                                      <code>PersistenceContextType.TRANSACTION</code>
	 */
	public <T> T merge(T entity)
	{
		return null;
	}

	/**
	 * Remove the entity instance.
	 * @param entity entity instance
	 * @throws IllegalArgumentException     if the instance is not an
	 *                                      entity or is a detached entity
	 * @throws TransactionRequiredException if invoked on a
	 *                                      container-managed entity manager of type
	 *                                      <code>PersistenceContextType.TRANSACTION</code> and there is
	 *                                      no transaction
	 */
	public void remove(Object entity)
	{

	}
}
