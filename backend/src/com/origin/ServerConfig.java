package com.origin;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class ServerConfig
{
	private static String WORK_DIR = "./";

	public static String DB_USER;

	public static void loadConfig()
	{
		File configFile = new File(WORK_DIR + "config/server.conf");

		Config conf = ConfigFactory
				.parseFile(configFile)
				.withFallback(ConfigFactory.load("server.defaults.conf"));

		DB_USER = conf.getString("db.user");
	}
}
