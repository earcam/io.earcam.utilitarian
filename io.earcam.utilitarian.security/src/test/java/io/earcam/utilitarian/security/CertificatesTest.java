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

import static io.earcam.utilitarian.security.Certificates.DN_LOCALHOST;
import static io.earcam.utilitarian.security.Certificates.certificate;
import static io.earcam.utilitarian.security.Certificates.CertificateBuilder.localDate;
import static io.earcam.utilitarian.security.Keys.rsa;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.Month;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class CertificatesTest {

	private static final KeyPair RSA = rsa();


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
		String name = "foo";

		X509Certificate x509 = certificate()
				.issuer(name)
				.subject("bar")
				.serial(2)
				.key(RSA)
				.toX509();

		assertThat(x509.getIssuerDN().getName(), is(equalTo("CN=" + name)));
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
}
