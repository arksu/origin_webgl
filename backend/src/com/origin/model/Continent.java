package com.origin.model;

/**
 * игровой континент (материк)
 * в игре может быть несколько больших континентов одновременно
 */
public class Continent
{
	private final int _index;

	private WorldRegion[] _grids;

	public Continent(int index)
	{
		_index = index;
	}
}
