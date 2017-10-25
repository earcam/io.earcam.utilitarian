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

import static io.earcam.utilitarian.net.security.KeyStores.deserialize;
import static io.earcam.utilitarian.net.security.KeyStores.keyStore;
import static io.earcam.utilitarian.net.security.KeyStores.rsa;
import static io.earcam.utilitarian.net.security.KeyStores.serialize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.junit.Test;

import io.earcam.utilitarian.net.security.Certificates;

public class KeyStoresTest {

	@Test
	public void test() throws Exception
	{
		char[] storePassword = "store.password".toCharArray();
		char[] aliasPassword = "alias.password".toCharArray();
		String alias = "this.is.an.alias";

		KeyPair pair = rsa();
		X509Certificate certificate = Certificates.localhostCertificate(pair);
		KeyStore keyStore = keyStore(alias, aliasPassword, pair, certificate);

		byte[] dehydrated = serialize(keyStore, storePassword);
		KeyStore hydrated = deserialize(dehydrated, storePassword);

		assertThat(hydrated.aliases().nextElement(), is(equalTo(alias)));

		Certificate certificate2 = hydrated.getCertificate(alias);

		assertThat(certificate2.getPublicKey().getEncoded(), is(equalTo(certificate.getPublicKey().getEncoded())));
	}
}
