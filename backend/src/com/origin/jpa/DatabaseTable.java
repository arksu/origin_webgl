package com.origin.jpa;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTable
{
	private String _name;

	private String _creationSuffix;

	private boolean _createOnDeploy = true;

	private boolean _truncateOnDeploy = false;

	private boolean _dropOnDeploy = false;

	private boolean _deploy = true;

	protected List<IndexDefinition> _indexes;

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public String getCreationSuffix()
	{
		return _creationSuffix;
	}

	public void setCreationSuffix(String creationSuffix)
	{
		_creationSuffix = creationSuffix;
	}

	public boolean isCreateOnDeploy()
	{
		return _createOnDeploy;
	}

	public void setCreateOnDeploy(boolean createOnDeploy)
	{
		_createOnDeploy = createOnDeploy;
	}

	public boolean isTruncateOnDeploy()
	{
		return _truncateOnDeploy;
	}

	public void setTruncateOnDeploy(boolean truncateOnDeploy)
	{
		_truncateOnDeploy = truncateOnDeploy;
	}

	public boolean isDropOnDeploy()
	{
		return _dropOnDeploy;
	}

	public void setDropOnDeploy(boolean dropOnDeploy)
	{
		_dropOnDeploy = dropOnDeploy;
	}

	public boolean isDeploy()
	{
		return _deploy;
	}

	public void setDeploy(boolean deploy)
	{
		_deploy = deploy;
	}

	public List<IndexDefinition> getIndexes()
	{
		if (_indexes == null)
		{
			_indexes = new ArrayList<>();
		}
		return _indexes;
	}

	public boolean haveIndexes()
	{
		return _indexes != null && _indexes.size() > 0;
	}

	public boolean checkExists(Connection connection) throws SQLException
	{
		String sql = "SHOW TABLES LIKE '" + _name + "'";
		Statement st = connection.createStatement();
		ResultSet rs = st.executeQuery(sql);
		return rs.isBeforeFirst();
	}
}
