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
}
