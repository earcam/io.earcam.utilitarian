/*-
 * #%L
 * io.earcam.utilitarian.security
 * %%
 * Copyright (C) 2017 - 2018 earcam
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

import static io.earcam.utilitarian.security.KeyStores.decode;
import static io.earcam.utilitarian.security.KeyStores.keyStore;
import static io.earcam.utilitarian.security.KeyStores.encode;
import static io.earcam.utilitarian.security.Keys.rsa;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.Test;

import io.earcam.utilitarian.security.Certificates;

public class KeyStoresTest {

	private final char[] storePassword = "store.password".toCharArray();
	private final char[] aliasPassword = "alias.password".toCharArray();
	private final String alias = "this.is.an.alias";

	private final KeyPair pair = rsa();

	private final KeyPairCredential credential = new KeyPairCredential(pair, alias, aliasPassword);
	private final X509Certificate certificate = Certificates.localhostCertificate(pair);

	private KeyStore keyStore = keyStore(credential, certificate);


	@Test
	public void keyPairFromStore()
	{
		KeyPair newPair = KeyStores.keyPair(keyStore, alias, aliasPassword);

		assertThat(newPair.getPrivate(), is(equalTo(pair.getPrivate())));
		assertThat(newPair.getPublic(), is(equalTo(pair.getPublic())));
	}


	@Test
	public void symmetricEncodeDecode() throws Exception
	{
		byte[] dehydrated = encode(keyStore, storePassword);
		KeyStore hydrated = decode(dehydrated, storePassword);

		assertThat(hydrated.aliases().nextElement(), is(equalTo(alias)));

		Certificate certificate2 = hydrated.getCertificate(alias);

		assertThat(certificate2.getPublicKey().getEncoded(), is(equalTo(certificate.getPublicKey().getEncoded())));
	}


	@Test
	public void throwsWithoutCertificates()
	{
		try {
			keyStore(credential);
			fail();
		} catch(IllegalArgumentException e) {}
	}
}
