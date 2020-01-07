package com.origin.entity;

import com.origin.jpa.ColumnExtended;
import com.origin.jpa.TableExtended;
import com.origin.utils.Utils;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * аккаунт пользователя к которому может прикрепляться несколько персонажей
 */
@Entity
@Table(name = "users", indexes = {
		@Index(name = "login_uniq", columnList = "login", unique = true)
})
@TableExtended(creationSuffix = "engine=MyISAM COMMENT='users'", drop = true, truncate = false)
public class User
{
	@Id
	@Column(name = "id", columnDefinition = "INT(11) NOT NULL AUTO_INCREMENT")
	@ColumnExtended(updateInsertId = true)
	private int _id;

	@Column(name = "login", columnDefinition = "VARCHAR(64) NOT NULL", nullable = false)
	private String _login;

	@Column(name = "password", columnDefinition = "VARCHAR(64) NOT NULL", nullable = false)
	private String _password = "123";

	@Column(name = "createTime", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
	private Timestamp _createTime;

	@Column(name = "ssid", columnDefinition = "CHAR(32) NULL DEFAULT NULL")
	private String _ssid;

	public int getId()
	{
		return _id;
	}

	public String getLogin()
	{
		return _login;
	}

	public void setLogin(String login)
	{
		_login = login;
	}

	public String getPassword()
	{
		return _password;
	}

	public void setPassword(String password)
	{
		_password = password;
	}

	public Timestamp getCreateTime()
	{
		return _createTime;
	}

	public String getSsid()
	{
		return _ssid;
	}

	public void setSsid(String ssid)
	{
		_ssid = ssid;
	}

	public void generateSessionId()
	{
		_ssid = Utils.generatString(32);
	}
}
