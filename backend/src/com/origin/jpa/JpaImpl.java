package com.origin.jpa;

import java.util.HashMap;
import java.util.Map;

public class JpaImpl
{
	private Map<Class, ClassDescriptor> _descriptors = new HashMap<>(4);

	public void addEntityClass(Class<?> clazz)
	{
		ClassDescriptor descriptor = new ClassDescriptor(clazz);
		_descriptors.put(clazz, descriptor);
	}

	public <T> T find(Class<T> entityClass, Object primaryKey)
	{

		return null;
	}
}
