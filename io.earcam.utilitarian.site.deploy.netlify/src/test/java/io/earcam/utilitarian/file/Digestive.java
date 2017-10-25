/*-
 * #%L
 * io.earcam.utilitarian.file
 * %%
 * Copyright (C) 2017 earcam
 * %%
 * SPDX-License-Identifier: (BSD-3-Clause OR EPL-1.0 OR Apache-2.0 OR MIT)
 * 
 * You <b>must</b> choose to accept, in full - any individual or combination of 
 * the following licenses:
 * <ul>
 * 	<li><a href="https://opensource.org/licenses/BSD-3-Clause">BSD-3-Clause</a></li>
 * 	<li><a href="https://www.eclipse.org/legal/epl-v10.html">EPL-1.0</a></li>
 * 	<li><a href="https://www.apache.org/licenses/LICENSE-2.0">Apache-2.0</a></li>
 * 	<li><a href="https://opensource.org/licenses/MIT">MIT</a></li>
 * </ul>
 * #L%
 */
package io.earcam.utilitarian.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Digestive {

	private static final String SHA1 = "SHA-1";
	private static final char[] HEXCODES = "0123456789ABCDEF".toCharArray();


	private Digestive()
	{}


	public static String sha1Hex(InputStream input)
	{
		return toHexString(sha1(input));
	}


	static byte[] sha1(InputStream input)
	{
		MessageDigest digest = sha1Algorithm();
		try(DigestInputStream digestable = new DigestInputStream(input, digest)) {
			while(digestable.read() != -1);
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
		return digest.digest();
	}


	private static MessageDigest sha1Algorithm()
	{
		return digestAlgorithm(SHA1);
	}


	static MessageDigest digestAlgorithm(String algorithm)
	{
		try {
			return MessageDigest.getInstance(algorithm);
		} catch(NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
	}


	public static String toHexString(byte[] data)
	{
		StringBuilder r = new StringBuilder(data.length * 2);
		for(byte b : data) {
			r.append(HEXCODES[(b >> 4) & 0xF]);
			r.append(HEXCODES[(b & 0xF)]);
		}
		return r.toString();
	}
}
