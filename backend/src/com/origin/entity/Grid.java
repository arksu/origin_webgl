package com.origin.entity;

import org.jpark.TableExtended;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.sql.Blob;

/**
 * игровой "чанк" (регион), базовый кусок карты
 */
@Entity
@Table(name = "grids", indexes = {
		@Index(name = "id_uniq", columnList = "supergrid, x, y, level", unique = true),
		@Index(name = "coord", columnList = "x, y, level")
})
@TableExtended(creationSuffix = "engine=MyISAM")
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

	@Column(name = "level", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _level;

	/**
	 * сырые данные тайлов в виде массива байт, по 2 байта на 1 тайл
	 */
	@Column(name = "tiles", columnDefinition = "BLOB NOT NULL", nullable = false)
	private Blob _tilesBlob;

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

	public int getLevel()
	{
		return _level;
	}

	public void setLevel(int level)
	{
		_level = level;
	}

	public Blob getTilesBlob()
	{
		return _tilesBlob;
	}

	public void setTilesBlob(Blob tilesBlob)
	{
		_tilesBlob = tilesBlob;
	}
}
