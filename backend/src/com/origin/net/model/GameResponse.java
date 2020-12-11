package com.origin.net.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.origin.utils.StringTypeAdapter;

public class GameResponse
{
	@SerializedName("id")
	private final int id;

	/**
	 * хитрая сериализация - строку парсим как json объект и передаем "как есть"
	 */
	@SerializedName("d")
	@JsonAdapter(StringTypeAdapter.class)
	private final Object data;

	@SerializedName("e")
	private final String errorText;

	@SerializedName("c")
	private final String channel;

	public GameResponse(int id, Object data)
	{
		this.id = id;
		this.data = data;
		this.errorText = null;
		this.channel = null;
	}

	public GameResponse(String channel, Object data)
	{
		this.id = 0;
		this.data = data;
		this.channel = channel;
		this.errorText = null;
	}

	public GameResponse(int id, String errorText)
	{
		this.id = id;
		this.data = null;
		this.channel = null;
		this.errorText = errorText;
	}
}

