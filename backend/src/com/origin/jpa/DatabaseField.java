package com.origin.jpa;

import javax.persistence.Column;
import java.util.HashMap;
import java.util.Map;

public class DatabaseField
{
	private static Map<Class, String> fieldTypeMapping = new HashMap<>();

	static
	{
		fieldTypeMapping.put(Boolean.class, "TINYINT(1) default 0");

		fieldTypeMapping.put(Integer.class, ("INTEGER"));
		fieldTypeMapping.put(Long.class, "BIGINT");
		fieldTypeMapping.put(Float.class, "FLOAT");
		fieldTypeMapping.put(Double.class, "DOUBLE");
		fieldTypeMapping.put(Short.class, "SMALLINT");
		fieldTypeMapping.put(Byte.class, "TINYINT");
		fieldTypeMapping.put(java.math.BigInteger.class, "BIGINT");
		fieldTypeMapping.put(java.math.BigDecimal.class, "DECIMAL");
		fieldTypeMapping.put(Number.class, "DECIMAL");

//		fieldTypeMapping.put(String.class, "NVARCHAR(255)"); // ??????
		fieldTypeMapping.put(String.class, "VARCHAR(255)");
		fieldTypeMapping.put(Character.class, "CHAR(1)");

		fieldTypeMapping.put(Byte[].class, "LONGBLOB");
		fieldTypeMapping.put(Character[].class, "LONGTEXT");
		fieldTypeMapping.put(byte[].class, "LONGBLOB");
		fieldTypeMapping.put(char[].class, "LONGTEXT");
		fieldTypeMapping.put(java.sql.Blob.class, "LONGBLOB");
		fieldTypeMapping.put(java.sql.Clob.class, "LONGTEXT");

		fieldTypeMapping.put(java.sql.Date.class, "DATE");
		fieldTypeMapping.put(java.sql.Time.class, "TIME");
		fieldTypeMapping.put(java.sql.Timestamp.class, "DATETIME");
		fieldTypeMapping.put(java.time.LocalDate.class, "DATE");
	}

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

	public DatabaseField(Class<?> type, Column annotation, DatabaseTable table)
	{
		_type = type;
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
			String stype = fieldTypeMapping.get(_type);
			s.append(stype);

		}
		return s.toString();
	}
}
