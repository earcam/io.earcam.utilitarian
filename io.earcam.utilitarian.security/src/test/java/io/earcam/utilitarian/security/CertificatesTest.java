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

//EARCAM_SNIPPET_BEGIN: imports
import static io.earcam.utilitarian.security.Certificates.DN_LOCALHOST;
import static io.earcam.utilitarian.security.Certificates.certificate;
import static io.earcam.utilitarian.security.Certificates.CertificateBuilder.localDate;
import static io.earcam.utilitarian.security.Keys.rsa;
import static java.time.ZoneId.systemDefault;
import static java.util.concurrent.TimeUnit.DAYS;
//EARCAM_SNIPPET_END: imports
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class CertificatesTest {

	private static final byte BYTE_ZERO = 0;
	// EARCAM_SNIPPET_BEGIN: keypair
	private static final KeyPair RSA = rsa();
	// EARCAM_SNIPPET_END: keypair


	@Test
	public void pemHasHeaderAndFooter()
	{
		String pem = certificate(RSA, "subject").toPem();

		MatcherAssert.assertThat(pem, allOf(
				startsWith("-----BEGIN CERTIFICATE-----\n"),
				endsWith("\n-----END CERTIFICATE-----\n")));
	}


	@Test
	public void localhost()
	{
		X509Certificate x509 = Certificates.localhostCertificate(RSA);

		// eh? Matchers.equalToIgnoringWhiteSpace() not working or one-sided?
		// assertThat(x509.getSubjectDN().getName(), is(equalToIgnoringWhiteSpace(DN_LOCALHOST)));
		assertThat(x509.getSubjectDN().getName(), is(equalTo(DN_LOCALHOST.replaceAll("\\s+", ""))));
	}


	@SuppressWarnings("deprecation")
	@Test
	public void deprecatedHostCertificateThrows()
	{
		try {
			Certificates.hostCertificate(RSA);
			fail();
		} catch(UnsupportedOperationException uoe) {}
	}


	@Test
	public void subjectName()
	{
		String name = "bar";

		X509Certificate x509 = certificate()
				.subject(name)
				.key(RSA)
				.toX509();

		assertThat(x509.getSubjectDN().getName(), is(equalTo("CN=" + name)));
	}


	@Test
	public void issuerName()
	{
		// EARCAM_SNIPPET_BEGIN: certificate
		X509Certificate x509 = certificate()
				.issuer("foo corp")
				.subject("bar cert")
				.key(RSA)
				.toX509();
		// EARCAM_SNIPPET_END: certificate

		assertThat(x509.getIssuerDN().getName(), is(equalTo("CN=" + "foo corp")));
	}


	@Test
	public void serial()
	{
		X509Certificate x509 = certificate(RSA, "subject")
				.serial(42)
				.toX509();

		assertThat(x509.getSerialNumber(), is(equalTo(BigInteger.valueOf(42))));
	}


	@Test
	public void signedBy()
	{
		String signatureAlgorithm = "SHA256withRSA";
		X509Certificate x509 = certificate(RSA, "subject")
				.signedBy(signatureAlgorithm)
				.toX509();

		assertThat(x509.getSigAlgName(), is(equalTo(signatureAlgorithm)));
	}


	@Test
	public void validFrom()
	{
		LocalDate from = LocalDate.of(2018, Month.MAY, 30);
		X509Certificate x509 = certificate(RSA, "subject")
				.validFrom(from)
				.toX509();

		assertThat(localDate(x509.getNotBefore()), is(equalTo(from)));
	}


	@Test
	public void validFor()
	{
		LocalDate from = LocalDate.now(systemDefault()).plus(1L, ChronoUnit.DAYS);
		X509Certificate x509 = certificate(RSA, "subject")
				.validFor(1, DAYS)
				.toX509();

		assertThat(localDate(x509.getNotAfter()), is(equalTo(from)));
	}


	@Test
	public void canNotSignOtherCertificates()
	{
		X509Certificate x509 = certificate()
				.issuer("hum")
				.subject("bug")
				.serial(2)
				.key(RSA)
				.toX509();

		byte[] extensionValue = x509.getExtensionValue(Certificates.CertificateBuilder.EXTENSION_MAY_ACT_AS_CA);

		assertThat(extensionValue[3], is(BYTE_ZERO));
	}


	@Test
	public void canSignOtherCertificates()
	{
		X509Certificate x509 = certificate()
				.issuer("hum")
				.subject("bug")
				.serial(2)
				.key(RSA)
				.canSignOtherCertificates()
				.toX509();

		byte[] extensionValue = x509.getExtensionValue(Certificates.CertificateBuilder.EXTENSION_MAY_ACT_AS_CA);

		assertThat(extensionValue[3], is(not(BYTE_ZERO)));
	}
}
