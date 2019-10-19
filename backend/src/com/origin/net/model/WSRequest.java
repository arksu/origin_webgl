package com.origin.net.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class WSRequest
{
	@SerializedName("id")
	public int id;

	@SerializedName("t")
	public String target;

	@SerializedName("d")
	public Map<String, Object> data;
}
