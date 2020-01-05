package com.origin.jpa;

import com.origin.jpa.helper.ClassConstants;
import com.origin.jpa.helper.Helper;

import java.io.CharArrayReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;

import static com.origin.jpa.helper.ClassConstants.*;

public class DatabasePlatform
{
	private static final int _stringBindingSize = 256;

	public static final char SEPARATE_CHAR = '`';

	/**
	 * INTERNAL
	 * Set the parameter in the JDBC statement.
	 * This support a wide range of different parameter types,
	 * and is heavily optimized for common types.
	 */
	public static void setParameterValue(Object parameter, PreparedStatement statement, int index) throws SQLException
	{
		// Process common types first.
		if (parameter instanceof String)
		{
			// Check for stream binding of large strings.
			if (((String) parameter).length() > _stringBindingSize)
			{
				CharArrayReader reader = new CharArrayReader(((String) parameter).toCharArray());
				statement.setCharacterStream(index, reader, ((String) parameter).length());
			}
			else
			{
//				statement.setNString(index, (String) parameter);
				statement.setString(index, (String) parameter);
			}
		}
		else if (parameter instanceof Number)
		{
			Number number = (Number) parameter;
			if (number instanceof Integer)
			{
				statement.setInt(index, number.intValue());
			}
			else if (number instanceof Long)
			{
				statement.setLong(index, number.longValue());
			}
			else if (number instanceof BigDecimal)
			{
				statement.setBigDecimal(index, (BigDecimal) number);
			}
			else if (number instanceof Double)
			{
				statement.setDouble(index, number.doubleValue());
			}
			else if (number instanceof Float)
			{
				statement.setFloat(index, number.floatValue());
			}
			else if (number instanceof Short)
			{
				statement.setShort(index, number.shortValue());
			}
			else if (number instanceof Byte)
			{
				statement.setByte(index, number.byteValue());
			}
			else if (number instanceof BigInteger)
			{
				// Convert to BigDecimal.
				statement.setBigDecimal(index, new BigDecimal((BigInteger) number));
			}
			else
			{
				statement.setObject(index, parameter);
			}
		}
		else if (parameter instanceof java.sql.Date)
		{
			statement.setDate(index, (java.sql.Date) parameter);
		}
		else if (parameter instanceof java.time.LocalDate)
		{
			statement.setDate(index, java.sql.Date.valueOf((java.time.LocalDate) parameter));
		}
		else if (parameter instanceof java.sql.Timestamp)
		{
			statement.setTimestamp(index, (java.sql.Timestamp) parameter);
		}
		else if (parameter instanceof java.time.LocalDateTime)
		{
			statement.setTimestamp(index, java.sql.Timestamp.valueOf((java.time.LocalDateTime) parameter));
		}
		else if (parameter instanceof java.time.OffsetDateTime)
		{
			statement.setTimestamp(index, java.sql.Timestamp.from(((java.time.OffsetDateTime) parameter).toInstant()));
		}
		else if (parameter instanceof java.sql.Time)
		{
			statement.setTime(index, (java.sql.Time) parameter);
		}
		else if (parameter instanceof java.time.LocalTime)
		{
			java.time.LocalTime lt = (java.time.LocalTime) parameter;
			java.sql.Timestamp ts = new java.sql.Timestamp(
					70, 0, 1, lt.getHour(), lt.getMinute(), lt.getSecond(), lt.getNano());
			statement.setTimestamp(index, ts);
		}
		else if (parameter instanceof java.time.OffsetTime)
		{
			java.time.OffsetTime ot = (java.time.OffsetTime) parameter;
			java.sql.Timestamp ts = new java.sql.Timestamp(
					70, 0, 1, ot.getHour(), ot.getMinute(), ot.getSecond(), ot.getNano());
			statement.setTimestamp(index, ts);
		}
		else if (parameter instanceof Boolean)
		{
			statement.setBoolean(index, (Boolean) parameter);
		}
		else if (parameter == null)
		{
			// Normally null is passed as a DatabaseField so the type is included, but in some case may be passed directly.
			statement.setNull(index, getJDBCType((Class) null));
		}
		else if (parameter instanceof DatabaseField)
		{
			int jdbcType = getJDBCType(((DatabaseField) parameter));
			statement.setNull(index, jdbcType);
		}
		else if (parameter instanceof byte[])
		{
			// TODO: возможно смотреть на длину переданного массива и делать сетап стрипа на ее основе
//			if (usesStreamsForBinding())
//			{
//				ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) parameter);
//				statement.setBinaryStream(index, inputStream, ((byte[]) parameter).length);
//			}
//			else
//			{
			statement.setBytes(index, (byte[]) parameter);
//			}
		}
		// Next process types that need conversion.
		else if (parameter instanceof Calendar)
		{
			statement.setTimestamp(index, Helper.timestampFromDate(((Calendar) parameter).getTime()));
		}
		else if (parameter.getClass() == ClassConstants.UTILDATE)
		{
			statement.setTimestamp(index, Helper.timestampFromDate((java.util.Date) parameter));
		}
		else if (parameter instanceof Character)
		{
			statement.setString(index, ((Character) parameter).toString());
		}
		else if (parameter instanceof char[])
		{
			statement.setString(index, new String((char[]) parameter));
		}
		// do not support this
//		else if (parameter instanceof Character[])
//		{
//			statement.setString(index, (String) convertObject(parameter, ClassConstants.STRING));
//		}
//		else if (parameter instanceof Byte[])
//		{
//			statement.setBytes(index, (byte[]) convertObject(parameter, ClassConstants.APBYTE));
//		}
		else if (parameter instanceof SQLXML)
		{
			statement.setSQLXML(index, (SQLXML) parameter);
		}
		else
		{
			statement.setObject(index, parameter);
		}
	}

	public static Object getObjectThroughOptimizedDataConversion(ResultSet resultSet, DatabaseField field, int columnNumber) throws SQLException
	{
		// тип колонки из базы
		final int type = resultSet.getMetaData().getColumnType(columnNumber);

		Object value = field;// Means no optimization, need to distinguish from null.
		Class fieldType = field.getType();

		if (type == Types.VARCHAR || type == Types.CHAR || type == Types.NVARCHAR || type == Types.NCHAR)
		{
			value = resultSet.getString(columnNumber);
			return value;
		}
		else if (fieldType == null)
		{
			return field;
		}

		boolean isPrimitive = false;

		// Optimize numeric values to avoid conversion into big-dec and back to primitives.
		if ((fieldType == ClassConstants.PLONG) || (fieldType == ClassConstants.LONG))
		{
			value = Long.valueOf(resultSet.getLong(columnNumber));
			isPrimitive = ((Long) value).longValue() == 0l;
		}
		else if ((fieldType == ClassConstants.INTEGER) || (fieldType == ClassConstants.PINT))
		{
			value = Integer.valueOf(resultSet.getInt(columnNumber));
			isPrimitive = ((Integer) value).intValue() == 0;
		}
		else if ((fieldType == ClassConstants.FLOAT) || (fieldType == ClassConstants.PFLOAT))
		{
			value = Float.valueOf(resultSet.getFloat(columnNumber));
			isPrimitive = ((Float) value).floatValue() == 0f;
		}
		else if ((fieldType == ClassConstants.DOUBLE) || (fieldType == ClassConstants.PDOUBLE))
		{
			value = Double.valueOf(resultSet.getDouble(columnNumber));
			isPrimitive = ((Double) value).doubleValue() == 0d;
		}
		else if ((fieldType == ClassConstants.SHORT) || (fieldType == ClassConstants.PSHORT))
		{
			value = Short.valueOf(resultSet.getShort(columnNumber));
			isPrimitive = ((Short) value).shortValue() == 0;
		}
		else if ((type == Types.TIME) || (type == Types.DATE) || (type == Types.TIMESTAMP))
		{
			// PERF: Optimize dates by calling direct get method if type is Date or Time,
			// unfortunately the double conversion is unavoidable for Calendar and util.Date.
			if (fieldType == ClassConstants.SQLDATE)
			{
				value = resultSet.getDate(columnNumber);
			}
			else if (fieldType == ClassConstants.TIME)
			{
				value = resultSet.getTime(columnNumber);
			}
			else if (fieldType == ClassConstants.TIMESTAMP)
			{
				value = resultSet.getTimestamp(columnNumber);
			}
			else if (fieldType == ClassConstants.TIME_LTIME)
			{
				final java.sql.Timestamp ts = resultSet.getTimestamp(columnNumber);
				value = ts != null ? ts.toLocalDateTime().toLocalTime() : null;
			}
			else if (fieldType == ClassConstants.TIME_LDATE)
			{
				final java.sql.Date dt = resultSet.getDate(columnNumber);
				value = dt != null ? dt.toLocalDate() : null;
			}
			else if (fieldType == ClassConstants.TIME_LDATETIME)
			{
				final java.sql.Timestamp ts = resultSet.getTimestamp(columnNumber);
				value = ts != null ? ts.toLocalDateTime() : null;
			}
			else if (fieldType == ClassConstants.TIME_OTIME)
			{
				final java.sql.Timestamp ts = resultSet.getTimestamp(columnNumber);
				value = ts != null ? ts.toLocalDateTime().toLocalTime().atOffset(java.time.OffsetDateTime.now().getOffset()) : null;
			}
			else if (fieldType == ClassConstants.TIME_ODATETIME)
			{
				final java.sql.Timestamp ts = resultSet.getTimestamp(columnNumber);
				value = ts != null ? java.time.OffsetDateTime.ofInstant(ts.toInstant(), java.time.ZoneId.systemDefault()) : null;
			}
		}
		else if (fieldType == ClassConstants.BIGINTEGER)
		{
			value = resultSet.getBigDecimal(columnNumber);
			if (value != null) return ((BigDecimal) value).toBigInteger();
		}
		else if (fieldType == ClassConstants.BIGDECIMAL)
		{
			value = resultSet.getBigDecimal(columnNumber);
		}

		// PERF: Only check for null for primitives.
		if (isPrimitive && resultSet.wasNull())
		{
			value = null;
		}

		return value;
	}

	/**
	 * Return the JDBC type for the Java type.
	 */
	public static int getJDBCType(Class javaType)
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

	/**
	 * Return the JDBC type for the given database field.
	 */
	public static int getJDBCType(DatabaseField field)
	{
		if (field != null)
		{
			return getJDBCType(ConversionManager.getObjectClass(field.getType()));
		}
		else
		{
			return Types.VARCHAR;
		}
	}

	/**
	 * INTERNAL:
	 * Clone the actual value represented by this mapping.  Do set the cloned value into the object.
	 */
	public static Object buildCloneValue(Object attributeValue)
	{
		Object newAttributeValue = attributeValue;
		if (attributeValue != null)
		{
			if (attributeValue instanceof byte[])
			{
				int length = ((byte[]) attributeValue).length;
				byte[] arrayCopy = new byte[length];
				System.arraycopy(attributeValue, 0, arrayCopy, 0, length);
				newAttributeValue = arrayCopy;
			}
			else if (attributeValue instanceof Byte[])
			{
				int length = ((Byte[]) attributeValue).length;
				Byte[] arrayCopy = new Byte[length];
				System.arraycopy(attributeValue, 0, arrayCopy, 0, length);
				newAttributeValue = arrayCopy;
			}
			else if (attributeValue instanceof char[])
			{
				int length = ((char[]) attributeValue).length;
				char[] arrayCopy = new char[length];
				System.arraycopy(attributeValue, 0, arrayCopy, 0, length);
				newAttributeValue = arrayCopy;
			}
			else if (attributeValue instanceof Character[])
			{
				int length = ((Character[]) attributeValue).length;
				Character[] arrayCopy = new Character[length];
				System.arraycopy(attributeValue, 0, arrayCopy, 0, length);
				newAttributeValue = arrayCopy;
			}
			else if (attributeValue instanceof Date)
			{
				newAttributeValue = ((Date) attributeValue).clone();
			}
			else if (attributeValue instanceof Calendar)
			{
				newAttributeValue = ((Calendar) attributeValue).clone();
			}
		}
		return newAttributeValue;
	}

	/**
	 * Compare the attribute values.
	 */
	public static boolean compareObjectValues(Object firstValue, Object secondValue)
	{
		// PERF: Check identity before conversion.
		if (firstValue == secondValue)
		{
			return true;
		}

		if ((firstValue != null) && (secondValue != null))
		{
			// PERF: Check equals first, as normally no change.
			// Also for serialization objects bytes may not be consistent, but equals may work (HashMap).
			if (firstValue.equals(secondValue))
			{
				return true;
			}
		}

/*
		// CR2114 - following two lines modified; getFieldValue() needs class as an argument
		firstValue = getFieldValue(firstValue, session);
		secondValue = getFieldValue(secondValue, session);
		// PERF:  Check identity/nulls before special type comparison.
		if (firstValue == secondValue) {
			return true;
		}


		// PERF: Check equals first, as normally no change.
		if (firstValue.equals(secondValue)) {
			return true;
		}
*/

		if ((firstValue == null) || (secondValue == null))
		{
			return false;
		}

		return Helper.comparePotentialArrays(firstValue, secondValue);
	}
}
