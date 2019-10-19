package com.origin.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.origin.utils.Utils;

import java.lang.reflect.Type;

/**
 * хитрая сериализация - строку парсим как json объект и передаем "как есть"
 */
public class StringTypeAdapter implements JsonSerializer<Object>
{
	@Override
	public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context)
	{
		// строку передаем как есть.
		if (src instanceof String)
		{
			String s = (String) src;
			if (!Utils.isEmpty(s) && s.contains("{"))
			{
				JsonParser parser = new JsonParser();
				return parser.parse(s).getAsJsonObject();
			}
			else
			{
				return context.serialize(src);
			}
		}
		else
		{
			return context.serialize(src);
		}
	}
}
