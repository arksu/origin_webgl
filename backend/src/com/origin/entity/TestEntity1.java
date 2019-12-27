package com.origin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "test1")
public class TestEntity1
{
	@Column(name = "text")
	private String _text;

	@Column(name = "intval")
	private int _intVal1;

	@Column(name = "blobval")
	private byte[] _blob;

	public String getText()
	{
		return _text;
	}

	public void setText(String text)
	{
		_text = text;
	}

	public int getIntVal1()
	{
		return _intVal1;
	}

	public void setIntVal1(int intVal1)
	{
		_intVal1 = intVal1;
	}

	public byte[] getBlob()
	{
		return _blob;
	}

	public void setBlob(byte[] blob)
	{
		_blob = blob;
	}
}
