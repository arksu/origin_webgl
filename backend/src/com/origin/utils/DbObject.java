package com.origin.utils;

import com.origin.Database;

/**
 * небольшая обертка для удобства вызова persist у экземпляра сущности
 */
public class DbObject
{
	public void persist()
	{
		Database.em().persist(this);
	}
}
