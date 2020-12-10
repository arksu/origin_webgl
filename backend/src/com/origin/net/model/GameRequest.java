package com.origin.net.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class GameRequest
{
	@SerializedName("id")
	int id = 0;

	@SerializedName("t")
	String target = null;

	@SerializedName("d")
	Map<String, Object> data;
}
