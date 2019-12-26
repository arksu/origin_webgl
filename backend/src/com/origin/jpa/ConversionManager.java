package com.origin.jpa;

import com.origin.jpa.helper.ClassConstants;

import java.util.HashMap;
import java.util.Map;

public class ConversionManager
{

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
	 * INTERNAL:
	 * This is used to determine the wrapper class for a primitive.
	 */
	public static Class getObjectClass(Class javaClass)
	{
		// Null means unknown always for classifications.
		if (javaClass == null)
		{
			return null;
		}

		if (javaClass.isPrimitive())
		{
			if (javaClass == ClassConstants.PCHAR)
			{
				return ClassConstants.CHAR;
			}
			if (javaClass == ClassConstants.PINT)
			{
				return ClassConstants.INTEGER;
			}
			if (javaClass == ClassConstants.PDOUBLE)
			{
				return ClassConstants.DOUBLE;
			}
			if (javaClass == ClassConstants.PFLOAT)
			{
				return ClassConstants.FLOAT;
			}
			if (javaClass == ClassConstants.PLONG)
			{
				return ClassConstants.LONG;
			}
			if (javaClass == ClassConstants.PSHORT)
			{
				return ClassConstants.SHORT;
			}
			if (javaClass == ClassConstants.PBYTE)
			{
				return ClassConstants.BYTE;
			}
			if (javaClass == ClassConstants.PBOOLEAN)
			{
				return ClassConstants.BOOLEAN;
			}
		}
		else if (javaClass == ClassConstants.APBYTE)
		{
			return ClassConstants.APBYTE;
		}
		else if (javaClass == ClassConstants.APCHAR)
		{
			return ClassConstants.APCHAR;
		}
		else
		{
			return javaClass;
		}

		return javaClass;
	}
}
