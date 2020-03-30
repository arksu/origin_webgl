package com.origin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * предмет в инвентаре
 */
@Entity
@Table(name = "inventory")
public class InventoryItem
{
	@Id
	@Column(name = "id", columnDefinition = "INT(11) NOT NULL AUTO_INCREMENT")
	private int _id;

	/**
	 * ид инвентаря (родителя, вещи в которой находится этот предмет
	 */
	@Column(name = "inventoryId", columnDefinition = "INT(11) NOT NULL")
	private int _inventoryId;

	/**
	 * тип предмета
	 */
	@Column(name = "type", columnDefinition = "INT(11) NOT NULL")
	private int _type;

	/**
	 * положение внутри инвентаря
	 */
	@Column(name = "x", columnDefinition = "INT(11) NOT NULL")
	private int _x;

	@Column(name = "y", columnDefinition = "INT(11) NOT NULL")
	private int _y;

	/**
	 * качество вещи
	 */
	@Column(name = "quality", columnDefinition = "INT(11) NOT NULL")
	private int _quality;

	/**
	 * количество в стаке
	 */
	@Column(name = "count", columnDefinition = "INT(11) NOT NULL")
	private int _count;

	/**
	 * тик (если вещь может имзенятся с течением времени
	 */
	@Column(name = "tick", columnDefinition = "INT(11) NOT NULL")
	private int _tick;

	public int getId()
	{
		return _id;
	}

	public int getInventoryId()
	{
		return _inventoryId;
	}

	public void setInventoryId(int inventoryId)
	{
		_inventoryId = inventoryId;
	}

	public int getType()
	{
		return _type;
	}

	public void setType(int type)
	{
		_type = type;
	}

	public int getX()
	{
		return _x;
	}

	public void setX(int x)
	{
		_x = x;
	}

	public int getY()
	{
		return _y;
	}

	public void setY(int y)
	{
		_y = y;
	}

	public int getQuality()
	{
		return _quality;
	}

	public void setQuality(int quality)
	{
		_quality = quality;
	}

	public int getCount()
	{
		return _count;
	}

	public void setCount(int count)
	{
		_count = count;
	}

	public int getTick()
	{
		return _tick;
	}

	public void setTick(int tick)
	{
		_tick = tick;
	}
}
