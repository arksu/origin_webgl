package com.origin;

import com.origin.net.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Locale;

public class Launcher
{
	private static final Logger _log = LoggerFactory.getLogger(Launcher.class.getName());

	public static void main(String... args)
	{
		Locale.setDefault(Locale.ROOT);

		ServerConfig.loadConfig();
		Database.start();

		_log.debug("start game server...");

		GameServer server = new GameServer(new InetSocketAddress("0.0.0.0", 7070), Runtime.getRuntime().availableProcessors());
		server.start();
	}
}
