package com.origin.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static com.origin.jpa.DatabasePlatform.SEPARATE_CHAR;

/**
 * дескриптор класса сущности
 * храним поля сущности, все нужное для DDL
 */
public class ClassDescriptor
{
	private static final Logger _log = LoggerFactory.getLogger(ClassDescriptor.class.getName());

	private Class<?> _javaClass;
	private String _javaClassName;

	private DatabaseTable _table;
	private List<DatabaseField> _primaryKeyFields;
	private List<DatabaseField> _fields;

	/**
	 * кэшируем SQL запросы для типовых операций по одному ключу
	 */
	private String _simpleInsertSql;
	private String _simpleSelectSql;
	private String _simpleDeleteSql;
	private Map<String, String> _selectOneSql;

	private Constructor<?> _defaultConstructor;

	public ClassDescriptor(Class<?> clazz)
	{
		_fields = new ArrayList<>(8);
		_primaryKeyFields = new ArrayList<>(2);

		_javaClass = clazz;
		_javaClassName = clazz.getName();
		_defaultConstructor = buildDefaultConstructorFor(_javaClass);

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

		// добавим все индексы в таблицу
		final Index[] indexes = tableAnnotation.indexes();
		if (indexes.length > 0)
		{
			for (Index index : indexes)
			{
				IndexDefinition indexDefinition = new IndexDefinition();
				indexDefinition.setName(index.name());
				indexDefinition.setUnique(index.unique());
				indexDefinition.getFields().addAll(Arrays.asList(index.columnList().split(",")));
				_table.getIndexes().add(indexDefinition);
			}
		}

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
			field.setAccessible(true);
			// ищем аннотации колонки таблицы
			Column column = field.getAnnotation(Column.class);
			ColumnExtended columnExtended = field.getAnnotation(ColumnExtended.class);
			DatabaseField columnField = null;
			if (column != null)
			{
				columnField = new DatabaseField(field, column, columnExtended, _table);
				_fields.add(columnField);
			}

			Id id = field.getAnnotation(Id.class);
			if (id != null)
			{
				DatabaseField idField;

				// если для поля определен Id но нет определения колонки - то построим колонку по аннотации Id
				if (column == null)
				{
					idField = new DatabaseField(field, _table);
					_fields.add(idField);
				}
				else
				{
					idField = columnField;
				}
				idField.setPrimaryKey(true);
				_primaryKeyFields.add(idField);
			}
		}
	}

	private String buildCreateSql()
	{
		StringBuilder sql = new StringBuilder("CREATE TABLE " + _table.getName() + " (");
		boolean isFirst = true;
		for (DatabaseField field : _fields)
		{
			if (!isFirst)
			{
				sql.append(", ");
			}
			sql.append(field.getCreateSql());
			isFirst = false;
		}

		if (_primaryKeyFields.size() > 0)
		{
			sql.append(", PRIMARY KEY (");
			isFirst = true;
			for (DatabaseField f : _primaryKeyFields)
			{
				if (!isFirst)
				{
					sql.append(", ");
				}
				sql.append(f.getName());
				isFirst = false;
			}
			sql.append(")");
		}

		if (_table.haveIndexes())
		{
			for (int i = 0; i < _table.getIndexes().size(); i++)
			{
				final IndexDefinition index = _table.getIndexes().get(i);
				if (index.isUnique())
				{
					sql.append(", UNIQUE KEY ");
				}
				else
				{
					sql.append(", KEY ");
				}
				String indexName = index.getName();
				if (indexName == null || indexName.length() == 0)
				{
					indexName = _table.getName() + "_uniq" + (i + 1);
				}
				sql.append(SEPARATE_CHAR).append(indexName).append(SEPARATE_CHAR).append(" (");
				for (int j = 0; j < index.getFields().size(); j++)
				{
					sql.append(index.getFields().get(j));
					if ((j + 1) < index.getFields().size())
					{
						sql.append(", ");
					}
				}
				sql.append(")");
			}
		}

		sql.append(")");
		if (_table.getCreationSuffix() != null && _table.getCreationSuffix().length() > 0)
		{
			sql.append(" ");
			sql.append(_table.getCreationSuffix());
		}

		return sql.toString();
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
					String sql = "DROP TABLE " + SEPARATE_CHAR + _table.getName() + SEPARATE_CHAR;
					_log.debug("execute SQL: " + sql);
					st.execute(sql);
					exists = false;
				}
				else if (_table.isTruncateOnDeploy())
				{
					String sql = "TRUNCATE TABLE " + SEPARATE_CHAR + _table.getName() + SEPARATE_CHAR;
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

	public String getSimpleInsertSql()
	{
		if (_simpleInsertSql == null)
		{
			StringBuilder sql = new StringBuilder("INSERT INTO ");
			sql.append(_table.getName());
			sql.append(" (");

			for (int i = 0; i < _fields.size(); i++)
			{
				final DatabaseField f = _fields.get(i);
				if (f.isInsertable())
				{
					sql.append(f.getName());
					if ((i + 1) < _fields.size())
					{
						sql.append(", ");
					}
				}
			}

			sql.append(") VALUES (");

			for (int i = 0; i < _fields.size(); i++)
			{
				if (_fields.get(i).isInsertable())
				{
					sql.append("?");
					if ((i + 1) < _fields.size())
					{
						sql.append(", ");
					}
				}
			}

			sql.append(")");
			_simpleInsertSql = sql.toString();
		}
		return _simpleInsertSql;
	}

	public String getSimpleSelectSql()
	{
		if (_simpleSelectSql == null)
		{
			StringBuilder sql = new StringBuilder("SELECT ");

			for (int i = 0; i < _fields.size(); i++)
			{
				sql.append(_fields.get(i).getName());
				if ((i + 1) < _fields.size())
				{
					sql.append(", ");
				}
			}
			sql.append(" FROM ")
			   .append(_table.getName())
			   .append(" WHERE ");

			// в этом методе ищем по 1 ключевому полю
			if (_primaryKeyFields.size() != 1)
			{
				throw new IllegalArgumentException("Wrong PK fields size, must be only 1 PK field");
			}
			sql.append(_primaryKeyFields.get(0).getName());
			sql.append("=?");
			_simpleSelectSql = sql.toString();
		}
		return _simpleSelectSql;
	}

	public String getSelectOneSql(String field)
	{
		if (_selectOneSql == null)
		{
			_selectOneSql = new HashMap<>();
		}

		String result = _selectOneSql.get(field);

		if (result == null)
		{
			StringBuilder sql = new StringBuilder("SELECT ");

			boolean found = false;
			for (int i = 0; i < _fields.size(); i++)
			{
				final String fname = _fields.get(i).getName();
				if (field.equals(fname))
				{
					found = true;
				}
				sql.append(fname);
				if ((i + 1) < _fields.size())
				{
					sql.append(", ");
				}
			}
			if (!found)
			{
				throw new RuntimeException("No such field in entity");
			}
			sql.append(" FROM ")
			   .append(_table.getName())
			   .append(" WHERE ");

			sql.append(field);
			sql.append("=?");
			result = sql.toString();
			_selectOneSql.put(field, result);
		}
		return result;
	}

	public String getSimpleDeleteSql()
	{
		if (_simpleDeleteSql == null)
		{
			StringBuilder sql = new StringBuilder("DELETE FROM ");
			sql.append(_table.getName())
			   .append(" WHERE ");
			if (_primaryKeyFields.size() != 1)
			{
				throw new IllegalArgumentException("Wrong PK fields size, must be only 1 PK field");
			}
			sql.append(_primaryKeyFields.get(0).getName());
			sql.append("=?");

			_simpleDeleteSql = sql.toString();
		}
		return _simpleDeleteSql;
	}

	public Class<?> getJavaClass()
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

	public List<DatabaseField> getPrimaryKeyFields()
	{
		return _primaryKeyFields;
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
			throw new RuntimeException(javaClass.getName() + " no such <Default Constructor>");
		}
	}

	public Object buildNewInstance()
	{
		try
		{
			return _defaultConstructor.newInstance();
		}
		catch (InstantiationException e)
		{
			throw new RuntimeException("InstantiationException", e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException("IllegalAccessException", e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException("InvocationTargetException", e);
		}
	}
}
