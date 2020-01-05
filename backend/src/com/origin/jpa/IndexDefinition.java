package com.origin.jpa;

import java.util.ArrayList;
import java.util.List;

/**
 * определяем индекс для таблицы
 */
public class IndexDefinition
{
	private String _name;
	private List<String> _fields;
	private boolean _isUnique;

	public IndexDefinition()
	{
		_fields = new ArrayList<>();
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public List<String> getFields()
	{
		return _fields;
	}

	public boolean isUnique()
	{
		return _isUnique;
	}

	public void setUnique(boolean unique)
	{
		_isUnique = unique;
	}
}
