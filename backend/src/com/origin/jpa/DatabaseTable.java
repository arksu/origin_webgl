package com.origin.jpa;

public class DatabaseTable
{
	private String _name;
	private String _creationSuffix;
	private boolean _mustBeCreated;

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

	public boolean isMustBeCreated()
	{
		return _mustBeCreated;
	}

	public void setMustBeCreated(boolean mustBeCreated)
	{
		_mustBeCreated = mustBeCreated;
	}
}
