package com.origin.model;

/**
 * игровое представление персонажа игрока в игровом мире
 */
public class Player extends GameObject
{
	/**
	 * персонаж игрока (сущность хранимая в БД)
	 */
	private final Character _character;

	/**
	 * одежда (во что одет игрок)
	 */
	private Paperdoll _paperdoll;

	public Player(Character character)
	{
		_character = character;
	}

	public Character getCharacter()
	{
		return _character;
	}

	@Override
	public int getX()
	{
		return 0;
	}

	@Override
	public int getY()
	{
		return 0;
	}

	@Override
	public int getLevel()
	{
		return 0;
	}

	public Paperdoll getPaperdoll()
	{
		return _paperdoll;
	}

	public void setPaperdoll(Paperdoll paperdoll)
	{
		_paperdoll = paperdoll;
	}
}
