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
	@Column(name = "id", columnDefinition = "INT(11) UNSIGNED NOT NULL AUTO_INCREMENT")
	@SerializedName("id")
	private int _id;

	/**
	 * ид аккаунта к которому привязан персонаж
	 */
	@Column(name = "accountId", columnDefinition = "INT(11) UNSIGNED NOT NULL", nullable = false)
	private transient int _accountId;

	/**
	 * имя персонажа (выводим на головой в игровом клиенте)
	 */
	@Column(name = "name", columnDefinition = "VARCHAR(16) NOT NULL", nullable = false)
	@SerializedName("name")
	private String _name;

	/**
	 * на каком континенте находится игрок, либо ид дома (инстанса, локации)
	 */
	@Column(name = "instanceId", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	@SerializedName("instanceId")
	private int _instanceId;

	/**
	 * координаты в игровых еденицах внутри континента (из этого расчитываем супергрид и грид)
	 */
	@Column(name = "x", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	@SerializedName("x")
	private int _x;

	@Column(name = "y", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	@SerializedName("y")
	private int _y;

	/**
	 * уровень (слой) глубины где находится игрок
	 */
	@Column(name = "level", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	@SerializedName("level")
	private int _level;

	/**
	 * когда был создан персонаж
	 */
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

	public int getAccountId()
	{
		return _accountId;
	}

	public void setAccountId(int accountId)
	{
		_accountId = accountId;
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

	public int getInstanceId()
	{
		return _instanceId;
	}

	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}

	public Timestamp getCreateTime()
	{
		return _createTime;
	}
}
