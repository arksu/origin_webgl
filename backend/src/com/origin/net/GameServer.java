package com.origin.net;

import com.origin.net.model.WSGameSession;

import java.net.InetSocketAddress;
import java.util.Map;

public class GameServer extends WSServer
{
	public GameServer(InetSocketAddress address, int decoderCount)
	{
		super(address, decoderCount);
	}

	@Override
	protected Object process(WSGameSession WSGameSession, String target, Map<String, Object> data) throws Exception
	{
		return null;
	}
}
