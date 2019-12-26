package com.origin.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class MyEntityManager
{
	private static final Logger _log = LoggerFactory.getLogger(MyEntityManager.class.getName());

	private Map<Class<?>, ClassDescriptor> _descriptors = new HashMap<>(4);

	private ConnectionFactory _connectionFactory;

	private Map<Object, Object> _cloneMap;
	private Map<Object, Object> _deletedObjects;

	public MyEntityManager()
	{
		_cloneMap = createMap();
		_deletedObjects = createMap();
	}

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
		// TODO
		connection.setAutoCommit(true);
		connection.commit();
		connection.close();
	}

	public void rollback(Connection connection) throws SQLException
	{
		// TODO
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
		final Connection c = _connectionFactory.get();
		for (ClassDescriptor descriptor : _descriptors.values())
		{
			descriptor.deploy(c);
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

		// ищем среди обслуживаемых сущностей такую
		Object clone = _cloneMap.get(entity);

		// если нашли - значит надо делать дифф и писать в базу апдейт
		if (clone != null)
		{
			// TODO
		}
		else
		{
			// не нашли. создаем новый объект в базе
			// формируем SQL запрос на инсерт
			StringBuilder sql = new StringBuilder("INSERT INTO ");
			sql.append(descriptor.getTable().getName());
			sql.append(" (");

			final List<DatabaseField> fields = descriptor.getFields();
			for (int i = 0; i < fields.size(); i++)
			{
				sql.append(fields.get(i).getName());
				if ((i + 1) < fields.size())
				{
					sql.append(", ");
				}
			}

			sql.append(") VALUES (");

			for (int i = 0; i < fields.size(); i++)
			{
				sql.append("?");
				if ((i + 1) < fields.size())
				{
					sql.append(", ");
				}
			}

			sql.append(")");

			try
			{
				try (PreparedStatement ps = connection.prepareStatement(sql.toString()))
				{
					// проходим по всем полям дескриптора
					for (int i = 0; i < fields.size(); i++)
					{
						Object val = fields.get(i).getField().get(entity);
						DatabasePlatform.setParameterValue(val, ps, i + 1);
					}
					_log.debug("execute insert SQL " + entity.toString() + ": " + sql);
					ps.executeUpdate();
					_cloneMap.put(entity, entity);
				}
			}
			catch (IllegalAccessException e)
			{
				_log.error("IllegalAccessException", e);
			}
			catch (SQLException e)
			{
				_log.error("SQLException", e);
			}
		}
	}

	public <T> T find(Class<T> entityClass, Object primaryKey)
	{
		return find(entityClass, _connectionFactory.get(), primaryKey);
	}

	public <T> T find(Class<T> entityClass, Connection connection, Object primaryKey)
	{
		ClassDescriptor descriptor = _descriptors.get(entityClass);
		if (descriptor == null)
		{
			throw new IllegalArgumentException("Not entity object, no class descriptor");
		}

		StringBuilder sql = new StringBuilder("SELECT ");

		final List<DatabaseField> fields = descriptor.getFields();
		for (int i = 0; i < fields.size(); i++)
		{
			sql.append(fields.get(i).getName());
			if ((i + 1) < fields.size())
			{
				sql.append(", ");
			}
		}
		sql.append(" FROM ").append(descriptor.getTable().getName());
		sql.append(" WHERE ");
		final List<DatabaseField> pkFields = descriptor.getPrimaryKeyFields();
		// в этом методе ищем по 1 ключевому полю
		if (pkFields.size() != 1)
		{
			throw new IllegalArgumentException("Wrong PK fields size, must be only 1 PK field");
		}
		sql.append(pkFields.get(0).getName());
		sql.append("=?");

		try
		{
			try (PreparedStatement ps = connection.prepareStatement(sql.toString()))
			{
				DatabasePlatform.setParameterValue(primaryKey, ps, 1);
				final ResultSet resultSet = ps.executeQuery();

				_log.debug(resultSet.toString());
			}
		}
//		catch (IllegalAccessException e)
//		{
//			_log.error("IllegalAccessException", e);
//		}
		catch (SQLException e)
		{
			_log.error("SQLException", e);
		}

		// создаем объект дефолтным конструктором
		// проходим по поляем объекта через дескриптор
		// получаем значения полей
		// пишем их в поля клона, используя buildCloneValue, т.е. значения тоже клоним если надо
		// запоминаем клона в мапе

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

	private Map<Object, Object> createMap()
	{
		return new IdentityHashMap<>();
	}

	public boolean isObjectDeleted(Object object)
	{
		return (_deletedObjects != null) && _deletedObjects.containsKey(object);
	}

	protected void undeleteObject(Object object)
	{
		_deletedObjects.remove(object);
	}
}
