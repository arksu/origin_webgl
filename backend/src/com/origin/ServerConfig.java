package com.origin;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class ServerConfig
{
	private static String WORK_DIR = "./";

	public static String DB_USER;
	public static String DB_PASSWORD;
	public static String DB_NAME;

	/**
	 * размер игрового мира (сколько континентов грузим из базы)
	 */
	public static int WORLD_CONTINENTS_SIZE;

	public static void loadConfig()
	{
		File configFile = new File(WORK_DIR + "config/server.conf");

		Config conf = ConfigFactory
				.parseFile(configFile)
				.withFallback(ConfigFactory.load("server.defaults.conf"));

		DB_USER = conf.getString("db.user");
		DB_PASSWORD = conf.getString("db.password");
		DB_NAME = conf.getString("db.name");

		WORLD_CONTINENTS_SIZE = conf.getInt("world.size");
	}
}
