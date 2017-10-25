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
package io.earcam.utilitarian.net.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class KeyStores {

	private KeyStores()
	{}


	public static KeyPair rsa() throws NoSuchAlgorithmException
	{
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024);
		return keyPairGenerator.generateKeyPair();
	}


	public static KeyStore keyStore(String alias, char[] aliasPassword, KeyPair pair, Certificate... certificates)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(null);
		keyStore.setKeyEntry(alias, pair.getPrivate(), aliasPassword, certificates);
		return keyStore;
	}


	public static byte[] serialize(KeyStore keyStore, char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		keyStore.store(output, password);
		return output.toByteArray();
	}


	public static KeyStore deserialize(byte[] dehydrated, char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		KeyStore rehydrated = KeyStore.getInstance("JKS");
		rehydrated.load(new ByteArrayInputStream(dehydrated), password);
		return rehydrated;
	}
}
