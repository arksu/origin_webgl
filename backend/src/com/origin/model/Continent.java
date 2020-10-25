package com.origin.model;

import com.origin.Database;
import com.origin.entity.Grid;

import java.util.List;

/**
 * игровой континент (материк)
 * в игре может быть несколько больших континентов одновременно
 */
public class Continent
{
	private final int _index;

	private Grid[] _grids;

	public Continent(int index)
	{
		// TODO
		List<Grid> all = Database.em().findAll(Grid.class, "SELECT * FROM grids WHERE supergrid=1");
		_index = index;
	}
}
