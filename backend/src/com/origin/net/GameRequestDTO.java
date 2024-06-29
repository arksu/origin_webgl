package com.origin.net;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * игровой запрос от клиента
 * java NOT kotlin из-за поля data
 * коряво десериализуется если поставить тип котлина Any
 */
public class GameRequestDTO {
    @SerializedName("id")
    public int id = 0;

    @SerializedName("t")
    public String target = null;

    @SerializedName("d")
    public Map<String, Object> data;
}
