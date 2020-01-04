package com.origin.jpa.helper;

public class Helper
{
	/**
	 * Answer a Timestamp from a java.util.Date.
	 */
	public static java.sql.Timestamp timestampFromDate(java.util.Date date)
	{
		return timestampFromLong(date.getTime());
	}

	/**
	 * Answer a Time from a long
	 * @param millis - milliseconds from the epoch (00:00:00 GMT
	 * Jan 1, 1970).  Negative values represent dates prior to the epoch.
	 */
	public static java.sql.Timestamp timestampFromLong(long millis)
	{
		java.sql.Timestamp timestamp = new java.sql.Timestamp(millis);

		// P2.0.1.3: Didn't account for negative millis < 1970
		// Must account for the jdk millis bug where it does not set the nanos.
		if ((millis % 1000) > 0)
		{
			timestamp.setNanos((int) (millis % 1000) * 1000000);
		}
		else if ((millis % 1000) < 0)
		{
			timestamp.setNanos((int) (1000000000 - (Math.abs((millis % 1000) * 1000000))));
		}
		return timestamp;
	}

	/**
	 * Compare two potential arrays and return true if they are the same. Will
	 * check for BigDecimals as well.
	 */
	public static boolean comparePotentialArrays(Object firstValue, Object secondValue)
	{
		Class firstClass = firstValue.getClass();
		Class secondClass = secondValue.getClass();

		// Arrays must be checked for equality because default does identity
		if ((firstClass == ClassConstants.APBYTE) && (secondClass == ClassConstants.APBYTE))
		{
			return compareByteArrays((byte[]) firstValue, (byte[]) secondValue);
		}
		else if ((firstClass == ClassConstants.APCHAR) && (secondClass == ClassConstants.APCHAR))
		{
			return compareCharArrays((char[]) firstValue, (char[]) secondValue);
		}
		else if ((firstClass.isArray()) && (secondClass.isArray()))
		{
			return compareArrays((Object[]) firstValue, (Object[]) secondValue);
		}
		else if (firstValue instanceof java.math.BigDecimal && secondValue instanceof java.math.BigDecimal)
		{
			// BigDecimals equals does not consider the precision correctly
			return compareBigDecimals((java.math.BigDecimal) firstValue, (java.math.BigDecimal) secondValue);
		}

		return false;
	}

	public static boolean compareArrays(Object[] array1, Object[] array2)
	{
		if (array1.length != array2.length)
		{
			return false;
		}
		for (int index = 0; index < array1.length; index++)
		{
			//Related to Bug#3128838 fix.  ! is added to correct the logic.
			if (array1[index] != null)
			{
				if (!array1[index].equals(array2[index]))
				{
					return false;
				}
			}
			else
			{
				if (array2[index] != null)
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Compare two BigDecimals.
	 * This is required because the .equals method of java.math.BigDecimal ensures that
	 * the scale of the two numbers are equal. Therefore 0.0 != 0.00.
	 * @see java.math.BigDecimal#equals(Object)
	 */
	public static boolean compareBigDecimals(java.math.BigDecimal one, java.math.BigDecimal two)
	{
		if (one.scale() != two.scale())
		{
			double doubleOne = (one).doubleValue();
			double doubleTwo = (two).doubleValue();
			if ((doubleOne != Double.POSITIVE_INFINITY) && (doubleOne != Double.NEGATIVE_INFINITY) && (doubleTwo != Double.POSITIVE_INFINITY) && (doubleTwo != Double.NEGATIVE_INFINITY))
			{
				return doubleOne == doubleTwo;
			}
		}
		return one.equals(two);
	}

	public static boolean compareByteArrays(byte[] array1, byte[] array2)
	{
		if (array1.length != array2.length)
		{
			return false;
		}
		for (int index = 0; index < array1.length; index++)
		{
			if (array1[index] != array2[index])
			{
				return false;
			}
		}
		return true;
	}

	public static boolean compareCharArrays(char[] array1, char[] array2)
	{
		if (array1.length != array2.length)
		{
			return false;
		}
		for (int index = 0; index < array1.length; index++)
		{
			if (array1[index] != array2[index])
			{
				return false;
			}
		}
		return true;
	}
}
