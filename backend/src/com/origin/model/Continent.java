package com.origin.model;

import com.origin.entity.Grid;

/**
 * игровой континент (материк)
 * в игре может быть несколько больших континентов одновременно
 */
public class Continent
{
	private final int _id;

	private Grid[] _grids;

	public Continent(int id)
	{
		_id = id;
	}
}
