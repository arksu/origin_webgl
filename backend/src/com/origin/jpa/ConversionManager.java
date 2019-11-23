package com.origin.jpa;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class ConversionManager
{
	public static final Class ABYTE = Byte[].class;
	public static final Class AOBJECT = Object[].class;
	public static final Class ACHAR = Character[].class;
	public static final Class APBYTE = byte[].class;
	public static final Class APCHAR = char[].class;
	public static final Class BIGDECIMAL = BigDecimal.class;
	public static final Class BIGINTEGER = BigInteger.class;
	public static final Class BOOLEAN = Boolean.class;
	public static final Class BYTE = Byte.class;
	public static final Class CLASS = Class.class;
	public static final Class CHAR = Character.class;
	public static final Class CALENDAR = Calendar.class;
	public static final Class DOUBLE = Double.class;
	public static final Class FLOAT = Float.class;
	public static final Class GREGORIAN_CALENDAR = GregorianCalendar.class;
	public static final Class INTEGER = Integer.class;
	public static final Class LONG = Long.class;
	public static final Class NUMBER = Number.class;
	public static final Class OBJECT = Object.class;
	public static final Class PBOOLEAN = boolean.class;
	public static final Class PBYTE = byte.class;
	public static final Class PCHAR = char.class;
	public static final Class PDOUBLE = double.class;
	public static final Class PFLOAT = float.class;
	public static final Class PINT = int.class;
	public static final Class PLONG = long.class;
	public static final Class PSHORT = short.class;
	public static final Class SHORT = Short.class;
	public static final Class SQLDATE = java.sql.Date.class;
	public static final Class STRING = String.class;
	public static final Class TIME = java.sql.Time.class;
	public static final Class TIMESTAMP = java.sql.Timestamp.class;
	public static final Class UTILDATE = java.util.Date.class;
	public static final Class TIME_LDATE = java.time.LocalDate.class;
	public static final Class TIME_LTIME = java.time.LocalTime.class;
	public static final Class TIME_LDATETIME = java.time.LocalDateTime.class;
	public static final Class TIME_ODATETIME = java.time.OffsetDateTime.class;
	public static final Class TIME_OTIME = java.time.OffsetTime.class;
	public static final Class QNAME = QName.class;
	public static final Class XML_GREGORIAN_CALENDAR = XMLGregorianCalendar.class;
	public static final Class DURATION = Duration.class;

	public static final Class BLOB = java.sql.Blob.class;
	public static final Class CLOB = java.sql.Clob.class;

	private static Map<Class, String> fieldTypeMapping;

	private static void buildFieldMapping()
	{
		fieldTypeMapping = new HashMap<>();

		fieldTypeMapping.put(boolean.class, "TINYINT(1) default 0");
		fieldTypeMapping.put(byte.class, "TINYINT");
		fieldTypeMapping.put(short.class, "SMALLINT");
		fieldTypeMapping.put(int.class, "INTEGER");
		fieldTypeMapping.put(long.class, "BIGINT");
		fieldTypeMapping.put(char.class, "CHAR(1)");
		fieldTypeMapping.put(double.class, "DOUBLE");
		fieldTypeMapping.put(float.class, "FLOAT");

		fieldTypeMapping.put(Boolean.class, "TINYINT(1) default 0");
		fieldTypeMapping.put(Integer.class, "INTEGER");
		fieldTypeMapping.put(Long.class, "BIGINT");
		fieldTypeMapping.put(Float.class, "FLOAT");
		fieldTypeMapping.put(Double.class, "DOUBLE");
		fieldTypeMapping.put(Short.class, "SMALLINT");
		fieldTypeMapping.put(Byte.class, "TINYINT");
		fieldTypeMapping.put(java.math.BigInteger.class, "BIGINT");
		fieldTypeMapping.put(java.math.BigDecimal.class, "DECIMAL");
		fieldTypeMapping.put(Number.class, "DECIMAL");

//		fieldTypeMapping.put(String.class, "NVARCHAR(255)"); // TODO: ??????
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

	public static String getFieldTypeDefinition(Class<?> javaType)
	{
		if (fieldTypeMapping == null)
		{
			buildFieldMapping();
		}
		return fieldTypeMapping.get(javaType);
	}

	/**
	 * Return the JDBC type for the Java type.
	 */
	public int getJDBCType(Class javaType)
	{
		if (javaType == TIME_ODATETIME)
		{
			return Types.TIMESTAMP;
		}
		else if (javaType == TIME_OTIME)
		{
			return Types.TIME;
		}

		if (javaType == null)
		{
			return Types.VARCHAR;// Best guess, sometimes we cannot determine type from mapping, this may fail on some drivers, other dont care what type it is.
		}
		else if (javaType == STRING)
		{
			return Types.VARCHAR;
		}
		else if (javaType == BIGDECIMAL)
		{
			return Types.DECIMAL;
		}
		else if (javaType == BIGINTEGER)
		{
			return Types.BIGINT;
		}
		else if (javaType == BOOLEAN)
		{
			return Types.BIT;
		}
		else if (javaType == BYTE)
		{
			return Types.TINYINT;
		}
		else if (javaType == CHAR)
		{
			return Types.CHAR;
		}
		else if (javaType == DOUBLE)
		{
			return Types.DOUBLE;
		}
		else if (javaType == FLOAT)
		{
			return Types.FLOAT;
		}
		else if (javaType == INTEGER)
		{
			return Types.INTEGER;
		}
		else if (javaType == LONG)
		{
			return Types.INTEGER;
		}
		else if (javaType == NUMBER)
		{
			return Types.DECIMAL;
		}
		else if (javaType == SHORT)
		{
			return Types.SMALLINT;
		}
		else if (javaType == CALENDAR)
		{
			return Types.TIMESTAMP;
		}
		else if (javaType == UTILDATE)
		{
			return Types.TIMESTAMP;
		}
		else if (javaType == TIME)
		{
			return Types.TIME;
		}
		else if (javaType == SQLDATE)
		{
			return Types.DATE;
		}
		else if (javaType == TIMESTAMP)
		{
			return Types.TIMESTAMP;
		}
		else if (javaType == ABYTE)
		{
			return Types.LONGVARBINARY;
		}
		else if (javaType == APBYTE)
		{
			return Types.LONGVARBINARY;
		}
		else if (javaType == BLOB)
		{
			return Types.BLOB;
		}
		else if (javaType == ACHAR)
		{
			return Types.LONGVARCHAR;
		}
		else if (javaType == APCHAR)
		{
			return Types.LONGVARCHAR;
		}
		else if (javaType == CLOB)
		{
			return Types.CLOB;
		}
		else
		{
			return Types.VARCHAR;// Best guess, sometimes we cannot determine type from mapping, this may fail on some drivers, other dont care what type it is.
		}
	}
}
