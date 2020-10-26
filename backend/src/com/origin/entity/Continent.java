package com.origin.entity;

import com.origin.Database;

import java.util.List;

/**
 * игровой континент (материк)
 * в игре может быть несколько больших континентов одновременно
 */
//@Entity
//@Table(name = "continents")
public class Continent
{
	private final int _id;

	private Grid[] _grids;

	public Continent(int id)
	{
		// TODO
		List<Grid> all = Database.em().findAll(Grid.class, "SELECT * FROM grids WHERE supergrid=1");
		_id = id;
	}
}
