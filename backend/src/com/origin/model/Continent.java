package com.origin.model;

import com.origin.entity.Grid;

import java.util.List;

/**
 * игровой континент (материк)
 * в игре может быть несколько больших континентов одновременно
 */
public class Continent
{
	private final int _id;

	private List<Grid> _grids;

	public Continent(int id)
	{
		_id = id;
	}
}
