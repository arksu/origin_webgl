package com.origin.utils;

import com.origin.Database;

public class DbObject
{
	public void persist()
	{
		Database.em().persist(this);
	}
}
