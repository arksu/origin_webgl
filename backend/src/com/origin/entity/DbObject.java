package com.origin.entity;

import com.origin.Database;

public class DbObject
{
	public void persist()
	{
		Database.em().persist(this);
	}
}
