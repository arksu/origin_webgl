package com.origin.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Utils
{
	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public static String generatString(int len)
	{
		char[] symbols = new char[]{'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f',
				'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
				'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
		};
		StringBuilder sb = new StringBuilder();
		Random random = ThreadLocalRandom.current();
		for (int i = 0; i < len; i++)
		{
			sb.append(symbols[random.nextInt(symbols.length)]);
		}
		return sb.toString();
	}

	public static String generatString(int len, char[] symbols)
	{
		StringBuilder sb = new StringBuilder();
		Random random = ThreadLocalRandom.current();
		for (int i = 0; i < len; i++)
		{
			sb.append(symbols[random.nextInt(symbols.length)]);
		}
		return sb.toString();
	}
}
