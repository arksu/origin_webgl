package com.origin.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * дескриптор класса сущности
 * храним поля сущности, все нужное для DDL
 */
public class ClassDescriptor
{
	private static final Logger _log = LoggerFactory.getLogger(ClassDescriptor.class.getName());

	private Class _javaClass;
	private String _javaClassName;

	private DatabaseTable _table;
	private List<DatabaseField> _primaryKeyFields;
	private List<DatabaseField> _fields;

	public ClassDescriptor(Class<?> clazz)
	{
		_fields = new ArrayList<>(8);
		_primaryKeyFields = new ArrayList<>(2);

		_javaClass = clazz;
		_javaClassName = clazz.getName();

		// проверим что переданный класс это сущность JPA
		Entity entity = clazz.getAnnotation(Entity.class);
		if (entity == null)
		{
			throw new IllegalArgumentException("no entity annotation for class: " + clazz.getName());
		}

		// читаем данные по таблице
		Table tableAnnotation = clazz.getAnnotation(Table.class);
		if (tableAnnotation == null)
		{
			throw new IllegalArgumentException("no table annotaion for class: " + clazz.getName());
		}
		_table = new DatabaseTable();
		_table.setName(tableAnnotation.name());

		// дополнительно суффикс создания таблицы
		TableExtended extendedData = clazz.getAnnotation(TableExtended.class);
		if (extendedData != null)
		{
			_table.setDeploy(extendedData.deploy());
			_table.setDropOnDeploy(extendedData.drop());
			_table.setCreateOnDeploy(extendedData.create());
			_table.setTruncateOnDeploy(extendedData.truncate());
			_table.setCreationSuffix(extendedData.creationSuffix());
		}

		// читаем поля класса
		for (Field field : clazz.getDeclaredFields())
		{
			// ищем аннотации колонки таблицы
			Column column = field.getAnnotation(Column.class);
			DatabaseField columnField = null;
			if (column != null)
			{
				columnField = new DatabaseField(field, column, _table);
				_fields.add(columnField);
			}

			Id id = field.getAnnotation(Id.class);
			if (id != null)
			{
				DatabaseField idField = new DatabaseField(field, _table);
				idField.setPrimaryKey(true);
				_primaryKeyFields.add(idField);

				// если для поля определен Id но нет определения колонки - то построим колонку по аннотации Id
				if (column == null)
				{
					_fields.add(idField);
				}
				else
				{
					idField.setName(columnField.getName());
				}
			}
		}
	}

	private String buildCreateSql()
	{
		StringBuilder s = new StringBuilder("CREATE TABLE " + _table.getName() + " (");
		boolean isFirst = true;
		for (DatabaseField field : _fields)
		{
			if (!isFirst)
			{
				s.append(", ");
			}
			s.append(field.getCreateSql());
			isFirst = false;
		}

		if (_primaryKeyFields.size() > 0)
		{
			s.append(", PRIMARY KEY (");
			isFirst = true;
			for (DatabaseField f : _primaryKeyFields)
			{
				if (!isFirst)
				{
					s.append(", ");
				}
				s.append(f.getName());
				isFirst = false;
			}
			s.append(")");
		}
		s.append(")");
		if (_table.getCreationSuffix() != null && _table.getCreationSuffix().length() > 0)
		{
			s.append(" ");
			s.append(_table.getCreationSuffix());
		}

		return s.toString();
	}

	public void deploy(Connection connection) throws SQLException
	{
		if (_table.isDeploy())
		{
			boolean exists = _table.checkExists(connection);
			_log.debug("deploy table: " + _table.getName() + ", exists: " + exists);
			Statement st = connection.createStatement();

			if (exists)
			{
				if (_table.isDropOnDeploy())
				{
					String sql = "DROP TABLE `" + _table.getName() + "`";
					_log.debug("execute SQL: " + sql);
					st.execute(sql);
					exists = false;
				}
				else if (_table.isTruncateOnDeploy())
				{
					String sql = "TRUNCATE TABLE `" + _table.getName() + "`";
					_log.debug("execute SQL: " + sql);
					st.execute(sql);
				}
			}

			if (_table.isCreateOnDeploy() && !exists)
			{
				String sql = buildCreateSql();
				_log.debug("execute SQL: " + sql);
				st.executeQuery(sql);
			}
		}
	}

	public Class getJavaClass()
	{
		return _javaClass;
	}

	public String getJavaClassName()
	{
		return _javaClassName;
	}

	public DatabaseTable getTable()
	{
		return _table;
	}

	public List<DatabaseField> getFields()
	{
		return _fields;
	}

	/**
	 * Build and return the default (zero-argument) constructor for the specified class.
	 */
	protected Constructor<?> buildDefaultConstructorFor(Class<?> javaClass)
	{
		try
		{
			Constructor<?> result = javaClass.getDeclaredConstructor();
			result.setAccessible(true);
			return result;
		}
		catch (NoSuchMethodException exception)
		{
			throw new RuntimeException(javaClass.getName() + " no such method <Default Constructor>");
		}
	}
}
