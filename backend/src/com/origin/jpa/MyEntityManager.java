package com.origin.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
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

			try
			{
				// будем писать в сущность сгенерированного ид только если у нас одно ключевое поле
				boolean isGeneratedOneKey = descriptor.getPrimaryKeyFields().size() == 1 && descriptor.getPrimaryKeyFields().get(0).isUpdateInsertId();

				try (PreparedStatement ps = isGeneratedOneKey ?
						connection.prepareStatement(descriptor.getSimpleInsertSql(), Statement.RETURN_GENERATED_KEYS) :
						connection.prepareStatement(descriptor.getSimpleInsertSql()))
				{
					// проходим по всем полям дескриптора
					final List<DatabaseField> fields = descriptor.getFields();
					for (int i = 0; i < fields.size(); i++)
					{
						Object val = fields.get(i).getField().get(entity);
						DatabasePlatform.setParameterValue(val, ps, i + 1);
					}
					_log.debug("execute insert SQL " + entity.toString() + ": " + descriptor.getSimpleInsertSql());
					int affectedRows = ps.executeUpdate();
					if (affectedRows == 0)
					{
						throw new SQLException("Insert failed, no affected rows");
					}
					if (isGeneratedOneKey)
					{
						try (ResultSet generatedKeys = ps.getGeneratedKeys())
						{
							if (generatedKeys.next())
							{
								final DatabaseField field = descriptor.getPrimaryKeyFields().get(0);
								final Object val = DatabasePlatform.getObjectThroughOptimizedDataConversion(generatedKeys, field, 1);

								ps.close();
								field.getField().set(entity, val);
							}
							else
							{
								throw new SQLException("insert user failed, no ID obtained.");
							}
						}
					}
					_cloneMap.put(entity, entity);
				}
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException("IllegalAccessException", e);
			}
			catch (SQLException e)
			{
				throw new RuntimeException("SQLException", e);
			}
		}
	}

	public <T> T find(Class<T> entityClass, Object primaryKeyValue)
	{
		return find(entityClass, _connectionFactory.get(), primaryKeyValue);
	}

	public <T> T find(Class<T> entityClass, Connection connection, Object primaryKeyValue)
	{
		ClassDescriptor descriptor = _descriptors.get(entityClass);
		if (descriptor == null)
		{
			throw new IllegalArgumentException("Not entity object, no class descriptor");
		}

		try
		{
			try (PreparedStatement ps = connection.prepareStatement(descriptor.getSimpleSelectSql()))
			{
				DatabasePlatform.setParameterValue(primaryKeyValue, ps, 1);
				_log.debug("execute select SQL " + entityClass.getName() + ": " + descriptor.getSimpleSelectSql());
				final ResultSet resultSet = ps.executeQuery();

				if (!resultSet.next())
				{
					throw new RuntimeException("Select return has no data");
				}

				// создаем объект дефолтным конструктором
				final Object workingCopy = descriptor.buildNewInstance();
				final Object clone = descriptor.buildNewInstance();

				final List<DatabaseField> fields = descriptor.getFields();
				// проходим по поляем объекта через дескриптор
				for (int i = 0; i < fields.size(); i++)
				{
					final DatabaseField field = fields.get(i);

					// получаем значения полей
					final Object val = DatabasePlatform.getObjectThroughOptimizedDataConversion(resultSet, field, i + 1);

					// пишем их в поля клона, используя buildCloneValue, т.е. значения тоже клоним если надо
					field.getField().set(workingCopy, val);
					field.getField().set(clone, DatabasePlatform.buildCloneValue(val));
				}

				// запоминаем клона в мапе
				_cloneMap.put(workingCopy, clone);

				return (T) workingCopy;
			}
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException("IllegalAccessException", e);
		}
		catch (SQLException e)
		{
			throw new RuntimeException("SQLException", e);
		}
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
