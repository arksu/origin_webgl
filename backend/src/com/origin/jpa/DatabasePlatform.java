package com.origin.jpa;

import com.origin.jpa.helper.ClassConstants;
import com.origin.jpa.helper.Helper;

import java.io.CharArrayReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.Calendar;

import static com.origin.jpa.helper.ClassConstants.*;

public class DatabasePlatform
{
	private static final int _stringBindingSize = 4096;

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
			statement.setTimestamp(index, org.eclipse.persistence.internal.helper.Helper.timestampFromDate(((Calendar) parameter).getTime()));
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
}
