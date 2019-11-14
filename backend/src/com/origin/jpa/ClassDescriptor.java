package com.origin.jpa;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * дескриптор класса сущности
 * храним поля сущности, все нужное для DDL
 */
public class ClassDescriptor
{
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

		// читаем данные по таблице
		Table tableAnnotation = clazz.getAnnotation(Table.class);
		if (tableAnnotation == null)
		{
			throw new IllegalArgumentException("no table annotaion");
		}
		_table = new DatabaseTable();
		_table.setName(tableAnnotation.name());

		// дополнительно суффикс создания таблицы
		TableExtended extendedData = clazz.getAnnotation(TableExtended.class);
		if (extendedData != null)
		{
			_table.setCreationSuffix(extendedData.creationSuffix());
			_table.setMustBeCreated(extendedData.create());
		}

		// читаем поля класса
		for (Field field : clazz.getDeclaredFields())
		{
			// ищем аннотации колонки таблицы
			Column column = field.getAnnotation(Column.class);
			if (column != null)
			{
				DatabaseField databaseField = new DatabaseField(field, column, _table);
				_fields.add(databaseField);
			}

			Id id = field.getAnnotation(Id.class);
			if (id != null)
			{
				DatabaseField databaseField = new DatabaseField(field, _table);
				_primaryKeyFields.add(databaseField);

				// если для поля определен Id но нет определения колонки - то построим колонку по аннотации Id
				if (column == null)
				{
					_fields.add(databaseField);
				}
			}
		}
	}

	public String buildCreateSql()
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
		// TODO index
		s.append(") ");
		s.append(_table.getCreationSuffix());

		return s.toString();
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
}
