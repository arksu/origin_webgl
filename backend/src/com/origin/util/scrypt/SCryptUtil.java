package com.origin.util.scrypt;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * Simple {@link SCrypt} interface for hashing passwords using the
 * <a href="http://www.tarsnap.com/scrypt.html">scrypt</a> key derivation function
 * and comparing a plain text password to a hashed one. The hashed output is an
 * extended implementation of the Modular Crypt Format that also includes the scrypt
 * algorithm parameters.
 * <p>
 * Format: <code>$s0$PARAMS$SALT$KEY</code>.
 *
 * <dl>
 * <dd>PARAMS</dd><dt>32-bit hex integer containing log2(N) (16 bits), r (8 bits), and p (8 bits)</dt>
 * <dd>SALT</dd><dt>base64-encoded salt</dt>
 * <dd>KEY</dd><dt>base64-encoded derived key</dt>
 * </dl>
 *
 * <code>s0</code> identifies version 0 of the scrypt format, using a 128-bit salt and 256-bit derived key.
 * @author Will Glozer
 */
public class SCryptUtil
{
	/**
	 * Compare the supplied plaintext password to a hashed password.
	 * @param passwd Plaintext password.
	 * @param hashed scrypt hashed password.
	 * @return true if passwd matches hashed value.
	 */
	public static boolean check(String passwd, String hashed)
	{
		try
		{
			String[] parts = hashed.split("\\$");

			if (parts.length != 5 || !parts[1].equals("s0"))
			{
				throw new IllegalArgumentException("Invalid hashed value");
			}

			long params = Long.parseLong(parts[2], 16);
			byte[] salt = Base64.decode(parts[3].toCharArray());
			byte[] derived0 = Base64.decode(parts[4].toCharArray());

			int N = (int) Math.pow(2, params >> 16 & 0xffff);
			int r = (int) params >> 8 & 0xff;
			int p = (int) params & 0xff;

			if (N != 2048 || r != 8 || p != 1)
			{
				throw new IllegalStateException("wrong scrypt params");
			}

			byte[] derived1 = SCrypt.scryptJ(passwd.getBytes(StandardCharsets.UTF_8), salt, N, r, p, 32);

			if (derived0.length != derived1.length) return false;

			int result = 0;
			for (int i = 0; i < derived0.length; i++)
			{
				result |= derived0[i] ^ derived1[i];
			}
			return result == 0;
		}
		catch (GeneralSecurityException e)
		{
			throw new IllegalStateException("JVM doesn't support SHA1PRNG or HMAC_SHA256?");
		}
	}
}
