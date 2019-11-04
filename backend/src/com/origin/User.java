package com.origin;

import com.origin.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * пользователь системы
 */
public class User
{
	private int _id;

	private String _password;

	private String _ssid;

	public int getId()
	{
		return _id;
	}

	public String getSsid()
	{
		return _ssid;
	}

	public void generateSessionId()
	{
		_ssid = Utils.generatString(32);
	}

	public boolean load(String login)
	{
		try
		{
			try (Connection con = Database.getConnection();
				 PreparedStatement st = con.prepareStatement("select * from users where login=?"))
			{
				st.setString(1, login);
				ResultSet rs = st.executeQuery();
				if (rs.next())
				{
					_id = rs.getInt("id");
					_password = rs.getString("password");
					return true;
				}
			}
		}
		catch (SQLException e)
		{
			return false;
		}
		return false;
	}
}
