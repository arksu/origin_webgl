package com.origin.entity;

import com.google.gson.annotations.SerializedName;
import com.origin.utils.DbObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * игровой персонаж игрока
 */
@Entity
@Table(name = "characters")
public class Character extends DbObject
{
	@Id
	@Column(name = "id", columnDefinition = "INT(11) NOT NULL AUTO_INCREMENT")
	@SerializedName("id")
	private int _id;

	@Column(name = "userId", columnDefinition = "INT(11) NOT NULL", nullable = false)
	private transient int _userId;

	@Column(name = "name", columnDefinition = "VARCHAR(16) NOT NULL", nullable = false)
	@SerializedName("name")
	private String _name;

	@Column(name = "x", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _x;

	@Column(name = "y", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _y;

	@Column(name = "level", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _level;

	@Column(name = "instanceId", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _intanceId;

	@Column(name = "createTime", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
	private transient Timestamp _createTime;

	public int getId()
	{
		return _id;
	}

	public void setId(int id)
	{
		_id = id;
	}

	public int getUserId()
	{
		return _userId;
	}

	public void setUserId(int userId)
	{
		_userId = userId;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
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

	public int getIntanceId()
	{
		return _intanceId;
	}

	public void setIntanceId(int intanceId)
	{
		_intanceId = intanceId;
	}

	public Timestamp getCreateTime()
	{
		return _createTime;
	}
}
