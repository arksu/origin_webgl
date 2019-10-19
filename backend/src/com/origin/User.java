package com.origin;

import com.origin.utils.Utils;

/**
 * пользователь системы
 */
public class User
{
	private int _id;

	private String _ssid;

	public int getId()
	{
		return _id;
	}

	public String getSsid()
	{
		return _ssid;
	}

	public void generateSessionId()
	{
		_ssid = Utils.generatString(32);
	}
}
