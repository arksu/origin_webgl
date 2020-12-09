package com.origin.net.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.origin.utils.StringTypeAdapter;

public class WSResponse
{
	@SerializedName("id")
	public int id;

	/**
	 * хитрая сериализация - строку парсим как json объект и передаем "как есть"
	 */
	@SerializedName("d")
	@JsonAdapter(StringTypeAdapter.class)
	public Object data;

	@SerializedName("e")
	public String errorText;

	@SerializedName("c")
	public String channel;
}
