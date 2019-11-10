package com.origin.jpa;

import javax.persistence.Column;

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

	public DatabaseField(Column annotation, DatabaseTable table)
	{
		_name = annotation.name();
		_qualifiedName = table.getName() + "." + _name;
		_isNullable = annotation.nullable();
		_columnDefinition = annotation.columnDefinition();
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
}
