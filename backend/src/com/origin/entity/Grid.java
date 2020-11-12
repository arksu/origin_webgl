package com.origin.entity;

import com.origin.utils.DbObject;
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
		@Index(name = "id_uniq", columnList = "instance, x, y, level", unique = true)
})
@TableExtended(creationSuffix = "engine=MyISAM")
public class Grid extends DbObject
{
	/**
	 * на каком континенте находится грид, либо ид дома (инстанса, локации)
	 */
	@Column(name = "instance", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _instanceId;

	/**
	 * координаты грида в мире (какой по счету грид, НЕ в игровых единицах)
	 * разбиение таблицы (partitions) делаем на основе RANGE(x) и субпартициях на основе RANGE(y)
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

	public int getInstanceId()
	{
		return _instanceId;
	}

	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
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
