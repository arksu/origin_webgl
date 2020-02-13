package com.origin.entity;

import com.google.gson.annotations.SerializedName;
import org.jpark.TableExtended;

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
//@TableExtended(truncate = true, drop = true)
public class Character
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
}
