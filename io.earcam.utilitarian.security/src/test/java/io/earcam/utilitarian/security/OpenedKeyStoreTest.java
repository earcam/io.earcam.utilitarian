/*-
 * #%L
 * io.earcam.utilitarian.security
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

import static io.earcam.utilitarian.security.KeyStores.keyStore;
import static io.earcam.utilitarian.security.Keys.rsa;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import org.junit.Test;

public class OpenedKeyStoreTest {

	private static final char[] aliasPassword = "alias.password".toCharArray();
	private static final String alias = "this.is.an.alias";

	private static final KeyPair pair = rsa();

	private static final X509Certificate certificate = Certificates.localhostCertificate(pair);

	private KeyStore keyStore = keyStore(alias, aliasPassword, pair, certificate);


	@Test
	public void propertiesAreAvailable()
	{
		@SuppressWarnings("resource")
		OpenedKeyStore opened = create();

		assertThat(opened.isDestroyed(), is(false));
		assertThat(opened.publicKey(), is(equalTo(pair.getPublic())));
		assertThat(opened.store(), is(equalTo(keyStore)));
	}


	private OpenedKeyStore create()
	{
		return new OpenedKeyStore(keyStore, new KeyPairCredential(pair, alias, aliasPassword));
	}


	@Test
	public void destroy()
	{
		@SuppressWarnings("resource")
		OpenedKeyStore opened = create();

		opened.destroy();

		assertThat(opened.isDestroyed(), is(true));
		assertThat(opened.store(), is(nullValue()));
	}


	@Test
	public void closingDestroys()
	{
		OpenedKeyStore opened = create();

		opened.close();

		assertThat(opened.isDestroyed(), is(true));
		assertThat(opened.store(), is(nullValue()));
	}
}
