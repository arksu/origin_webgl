package com.origin.util.scrypt;

import javax.crypto.Mac;
import java.security.GeneralSecurityException;

import static java.lang.System.arraycopy;

/**
 * An implementation of the Password-Based Key Derivation Function as specified
 * in RFC 2898.
 * @author Will Glozer
 */
public class PBKDF
{
	/**
	 * Implementation of PBKDF2 (RFC2898).
	 * @param mac Pre-initialized {@link Mac} instance to use.
	 * @param S Salt.
	 * @param c Iteration count.
	 * @param DK Byte array that derived key will be placed in.
	 * @param dkLen Intended length, in octets, of the derived key.
	 */
	public static void pbkdf2(Mac mac, byte[] S, int c, byte[] DK, int dkLen) throws GeneralSecurityException
	{
		int hLen = mac.getMacLength();

		if (dkLen > (Math.pow(2, 32) - 1) * hLen)
		{
			throw new GeneralSecurityException("Requested key length too long");
		}

		byte[] U = new byte[hLen];
		byte[] T = new byte[hLen];
		byte[] block1 = new byte[S.length + 4];

		int l = (int) Math.ceil((double) dkLen / hLen);
		int r = dkLen - (l - 1) * hLen;

		arraycopy(S, 0, block1, 0, S.length);

		for (int i = 1; i <= l; i++)
		{
			block1[S.length] = (byte) (i >> 24 & 0xff);
			block1[S.length + 1] = (byte) (i >> 16 & 0xff);
			block1[S.length + 2] = (byte) (i >> 8 & 0xff);
			block1[S.length + 3] = (byte) (i & 0xff);

			mac.update(block1);
			mac.doFinal(U, 0);
			arraycopy(U, 0, T, 0, hLen);

			for (int j = 1; j < c; j++)
			{
				mac.update(U);
				mac.doFinal(U, 0);

				for (int k = 0; k < hLen; k++)
				{
					T[k] ^= U[k];
				}
			}

			arraycopy(T, 0, DK, (i - 1) * hLen, (i == l ? r : hLen));
		}
	}
}
