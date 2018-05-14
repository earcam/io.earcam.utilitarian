/*-
 * #%L
 * io.earcam.utilitarian.net
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
package io.earcam.utilitarian.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import io.earcam.unexceptional.Exceptional;
import io.earcam.unexceptional.UncheckedSecurityException;

public class Keys {

	private static final int DEFAULT_KEYSIZE = 1024;


	private Keys()
	{}


	/**
	 * RSA key pair of size {@value #DEFAULT_KEYSIZE}
	 * 
	 * @return a key pair generated with RSA
	 * @throws UncheckedSecurityException if a {@link java.security.NoSuchAlgorithmException} is thrown
	 */
	public static KeyPair rsa()
	{
		return keyPair("RSA");
	}


	/**
	 * Generate an asymmetric key pair of size {@value #DEFAULT_KEYSIZE} for the supplied {@code algorithm}
	 * 
	 * @param algorithm e.g. RSA, DSA
	 * @return a key pair generated with the supplied {@code algorithm}
	 * @throws UncheckedSecurityException if a {@link java.security.NoSuchAlgorithmException} is thrown
	 */
	public static KeyPair keyPair(String algorithm)
	{
		return keyPair(algorithm, DEFAULT_KEYSIZE);
	}


	/**
	 * Generate an asymmetric key pair of size {@code keysize} for the supplied {@code algorithm}
	 * 
	 * @param algorithm e.g. RSA, DSA
	 * @param keysize
	 * @return a key pair generated with the supplied {@code algorithm}
	 * @throws UncheckedSecurityException if a {@link java.security.NoSuchAlgorithmException} is thrown
	 */
	public static KeyPair keyPair(String algorithm, int keysize)
	{
		KeyPairGenerator keyPairGenerator = Exceptional.apply(KeyPairGenerator::getInstance, algorithm);
		keyPairGenerator.initialize(keysize);
		return keyPairGenerator.generateKeyPair();
	}
}
