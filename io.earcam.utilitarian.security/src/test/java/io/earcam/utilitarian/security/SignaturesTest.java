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

import static io.earcam.utilitarian.security.Certificates.certificate;
import static io.earcam.utilitarian.security.KeyStores.keyStore;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.earcam.unexceptional.UncheckedSecurityException;

public class SignaturesTest {

	private static final KeyPair KEYS = Keys.rsa();
	private static final String ALIAS = "aka";
	private static final char[] PASSWORD = "open sesame".toCharArray();
	private static final String SUBJECT = "subject goes here";
	private static final X509Certificate CERTIFICATE = certificate(KEYS, SUBJECT).toX509();
	private static final KeyStore KEYSTORE = keyStore(ALIAS, PASSWORD, KEYS, CERTIFICATE);
	private static final OpenedKeyStore OPENED_KEYSTORE = new OpenedKeyStore(KEYSTORE, ALIAS, PASSWORD);

	private static final byte[] contents = "blah blah".getBytes(UTF_8);


	@Test
	public void symmetric()
	{
		byte[] signed = Signatures.sign(contents, OPENED_KEYSTORE, "SHA512withRSA");

		List<X509Certificate> certificates = Signatures.certificatesFromSignature(signed);

		assertThat(certificates, contains(CERTIFICATE));
	}


	@Test
	public void signingWithUnrecognisedAlgorithmThrows()
	{
		try {
			Signatures.sign(contents, OPENED_KEYSTORE, "SlimeAndSnailsWithPuppyDogsTails");
			fail();
		} catch(UncheckedSecurityException e) {}
	}


	@Test
	public void attemptToExtractCertificateFromGibberishSignatureThrows()
	{
		try {
			byte[] gibberish = "Dance Magic Dance".getBytes(UTF_8);
			Signatures.certificatesFromSignature(gibberish);
			fail();
		} catch(UncheckedSecurityException e) {}
	}
}
