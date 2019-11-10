package com.origin.jpa;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
			Column columnAnnotation = field.getAnnotation(Column.class);
			if (columnAnnotation != null)
			{
				DatabaseField databaseField = new DatabaseField(columnAnnotation, _table);
				_fields.add(databaseField);
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
}
