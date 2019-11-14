package com.origin.jpa;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * описание поля в бд
 */
public class DatabaseField
{

	/**
	 * Variables used for generating DDL
	 **/
//	protected int _scale;
//	protected int _length;
//	protected int _precision;
//	protected boolean _isUnique;
	protected boolean _isNullable;
	//	protected boolean _isUpdatable;
//	protected boolean _isPrimaryKey;
	protected String _columnDefinition;

	private Class<?> _type;

	/**
	 * Column name of the field.
	 */
	protected String _name;

	/**
	 * PERF: Cache fully qualified table.field-name.
	 */
	protected String _qualifiedName;

	/**
	 * Fields table (encapsulates name + creator).
	 */
	protected DatabaseTable _table;

	private Field _field;

	public DatabaseField(Field field, Column annotation, DatabaseTable table)
	{
		_field = field;
		_type = _field.getType();
		_name = annotation.name();
		if (_name.length() == 0)
		{
			_name = _field.getName().toUpperCase();
		}
		_qualifiedName = table.getName() + "." + _name;
		_isNullable = annotation.nullable();
		_columnDefinition = annotation.columnDefinition();
	}

	public DatabaseField(Field field, DatabaseTable table)
	{
		_field = field;
		_type = _field.getType();
		_name = _field.getName().toUpperCase();
		_qualifiedName = table.getName() + "." + _name;
		_isNullable = false;
		_table = table;
	}

	public DatabaseTable getTable()
	{
		return _table;
	}

	public String getName()
	{
		return _name;
	}

	public String getQualifiedName()
	{
		return _qualifiedName;
	}

	public boolean isNullable()
	{
		return _isNullable;
	}

	public String getColumnDefinition()
	{
		return _columnDefinition;
	}

	public String getCreateSql()
	{
		StringBuilder s = new StringBuilder(_name);
		s.append(" ");
		// если у нас явно определен тип колонки тупо прокинем его
		if (_columnDefinition != null && _columnDefinition.length() != 0)
		{
			s.append(_columnDefinition);
		}
		else
		{
			// иначе надо определить тип колонки по типу поля в сущности
			// TODO
			String stype = ConversionManager.getFieldTypeDefinition(_type);
			s.append(stype);
			if (_isNullable)
			{
				s.append(" NULL");
			}
			else
			{
				s.append(" NOT NULL");
			}
		}
		return s.toString();
	}
}
