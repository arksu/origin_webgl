package com.origin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.sql.Blob;

/**
 * игровой "чанк" (регион) 100 на 100 тайлов
 */
@Entity
@Table(name = "grids", indexes = {
		@Index(name = "id_uniq", columnList = "supergrid, x, y", unique = true)
})
public class Grid
{
	/**
	 * ид супергрида, по нему потом сделаем разбиение таблицы на партиции
	 * континент к которому оносится супергрид также зашит в ид супергрида
	 */
	@Column(name = "supergrid", columnDefinition = "INT(11) UNSIGNED NOT NULL", nullable = false)
	private int _supergrid;

	/**
	 * координаты грида внутри супергрида
	 */
	@Column(name = "x", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _x;

	@Column(name = "y", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _y;

	/**
	 * сырые данные тайлов в виде массива байт, по 2 байта на 1 тайл
	 */
	@Column(name = "data", columnDefinition = "BLOB NOT NULL", nullable = false)
	private Blob _data;

	public int getSupergrid()
	{
		return _supergrid;
	}

	public void setSupergrid(int supergrid)
	{
		_supergrid = supergrid;
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

	public Blob getData()
	{
		return _data;
	}

	public void setData(Blob data)
	{
		_data = data;
	}
}
