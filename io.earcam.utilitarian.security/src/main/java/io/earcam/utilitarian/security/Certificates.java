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

import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import io.earcam.unexceptional.Closing;
import io.earcam.unexceptional.Exceptional;

@ParametersAreNonnullByDefault
public class Certificates {

	private static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();
	static final String DN_LOCALHOST = "DN=localhost, L=London, C=GB";

	public static class CertificateBuilder {

		@SuppressWarnings("squid:S1313") // SonarQube false-positive; not an IP address
		private static final String EXTENSION_KEY_USAGE = "2.5.29.15";
		@SuppressWarnings("squid:S1313") // SonarQube false-positive; not an IP address
		static final String EXTENSION_MAY_ACT_AS_CA = "2.5.29.19";
		private String issuerName = "acme";
		private String subjectName;
		private BigInteger serial = BigInteger.ONE;
		private boolean canSignOtherCertificates = false;
		private LocalDate validFrom = LocalDate.now(ZoneId.systemDefault());
		private long duration = 365;
		private TimeUnit unit = TimeUnit.DAYS;
		private String signatureAlgorithm = "SHA256withRSA";
		private KeyPair keyPair;


		CertificateBuilder()
		{}


		public CertificateBuilder issuer(String name)
		{
			issuerName = name;
			return this;
		}


		public CertificateBuilder subject(String name)
		{
			subjectName = name;
			return this;
		}


		public CertificateBuilder serial(int number)
		{
			return serial(BigInteger.valueOf(number));
		}


		public CertificateBuilder serial(BigInteger number)
		{
			this.serial = number;
			return this;
		}


		public CertificateBuilder canSignOtherCertificates()
		{
			canSignOtherCertificates = true;
			return this;
		}


		public CertificateBuilder key(KeyPair pair)
		{
			this.keyPair = pair;
			return this;
		}


		public CertificateBuilder signedBy(String signatureAlgorithm)
		{
			this.signatureAlgorithm = signatureAlgorithm;
			return this;
		}


		public CertificateBuilder validFrom(LocalDate from)
		{
			validFrom = from;
			return this;
		}


		public CertificateBuilder validFor(long duration, TimeUnit unit)
		{
			this.duration = duration;
			this.unit = unit;
			return this;
		}


		public X509Certificate toX509()
		{
			Objects.requireNonNull(keyPair, "keyPair");
			Objects.requireNonNull(issuerName, "issuerName");
			Objects.requireNonNull(subjectName, "subjectName");
			X500Name issuer = new X500Name(addCnIfMissing(issuerName));
			X500Name subject = new X500Name(addCnIfMissing(subjectName));

			Date from = javaDate(validFrom);
			Date to = new Date(from.getTime() + unit.toMillis(duration));

			X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
					issuer,
					serial,
					from,
					to,
					subject,
					keyPair.getPublic());

			Exceptional.accept(this::addExtensions, certificateBuilder);

			X509CertificateHolder signed = sign(keyPair, signatureAlgorithm, certificateBuilder);

			JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(PROVIDER);
			return Exceptional.apply(converter::getCertificate, signed);
		}


		static Date javaDate(LocalDate date)
		{
			return java.sql.Date.valueOf(date);
		}


		static LocalDate localDate(Date date)
		{
			return new java.sql.Date(date.getTime()).toLocalDate();
		}


		private void addExtensions(X509v3CertificateBuilder certificateBuilder) throws CertIOException
		{
			certificateBuilder.addExtension(
					new ASN1ObjectIdentifier(EXTENSION_MAY_ACT_AS_CA),
					false,
					new BasicConstraints(canSignOtherCertificates)).addExtension(
							new ASN1ObjectIdentifier(EXTENSION_KEY_USAGE),
							true,
							new X509KeyUsage(
									X509KeyUsage.digitalSignature |
											X509KeyUsage.nonRepudiation |
											X509KeyUsage.keyEncipherment |
											X509KeyUsage.dataEncipherment));
		}


		private String addCnIfMissing(String name)
		{
			return (name.indexOf('=') == -1) ? "CN=" + name : name;
		}


		private static X509CertificateHolder sign(KeyPair keyPair, String signatureAlgorithm, X509v3CertificateBuilder certificateBuilder)
		{
			JcaContentSignerBuilder jcaContentSignerBuilder = new JcaContentSignerBuilder(signatureAlgorithm);
			ContentSigner sigGen = Exceptional.apply(jcaContentSignerBuilder::build, keyPair.getPrivate());
			return certificateBuilder.build(sigGen);
		}


		public String toPem()
		{
			StringWriter writer = new StringWriter();
			toPem(writer);
			return writer.toString();
		}


		public void toPem(Writer writer)
		{
			Closing.closeAfterAccepting(JcaPEMWriter::new, writer, toX509(), JcaPEMWriter::writeObject);
		}

	}


	private Certificates()
	{}


	public static CertificateBuilder certificate(KeyPair pair, String subjectName)
	{
		return certificate(pair)
				.subject(subjectName);
	}


	public static CertificateBuilder certificate(KeyPair pair)
	{
		return certificate().key(pair);
	}


	public static CertificateBuilder certificate()
	{
		return new CertificateBuilder();
	}


	public static X509Certificate localhostCertificate(KeyPair keys)
	{
		return hostCertificate(keys, DN_LOCALHOST);
	}


	/**
	 * @deprecated
	 * @see #localhostCertificate(KeyPair)
	 */
	@Deprecated
	public static X509Certificate hostCertificate(KeyPair keys)
	{
		throw new UnsupportedOperationException();
	}


	public static X509Certificate hostCertificate(KeyPair keys, String hostname)
	{
		return certificate(keys)
				.subject(hostname)
				.toX509();
	}
}
