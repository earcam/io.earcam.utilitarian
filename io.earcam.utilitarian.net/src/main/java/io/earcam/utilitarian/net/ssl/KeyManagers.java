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
package io.earcam.utilitarian.net.ssl;

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;

/**
 * Key manager
 *
 */
public final class KeyManagers {

	private static final class DummyX509KeyManager extends X509ExtendedKeyManager {

		private static final String[] NADDA = new String[0];


		private DummyX509KeyManager()
		{}


		@Override
		public String[] getClientAliases(String arg0, Principal[] arg1)
		{
			return NADDA;
		}


		@Override
		public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2)
		{
			return null;
		}


		@Override
		public String[] getServerAliases(String arg0, Principal[] arg1)
		{
			return NADDA;
		}


		@Override
		public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2)
		{
			return null;
		}


		@Override
		public X509Certificate[] getCertificateChain(String arg0)
		{
			return new X509Certificate[0];
		}


		@Override
		public PrivateKey getPrivateKey(String arg0)
		{
			return null;
		}
	}

	private static final X509ExtendedKeyManager NOOP_KEY_MANAGER = new DummyX509KeyManager();


	private KeyManagers()
	{}


	public static KeyManager[] keyManagerSunX509(KeyStore keyStore, char[] storePassword)
			throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException
	{
		return keyManager(keyStore, storePassword, "SunX509");
	}


	public static KeyManager[] keyManager(KeyStore keyStore, char[] storePassword, String algorithm)
			throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException
	{
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
		kmf.init(keyStore, storePassword);
		return kmf.getKeyManagers();
	}


	public static KeyManager[] keyManagerDummy()
	{
		return new KeyManager[] { NOOP_KEY_MANAGER };
	}
}
