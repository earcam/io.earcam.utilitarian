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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedDataParser;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import io.earcam.unexceptional.Exceptional;
import io.earcam.unexceptional.UncheckedSecurityException;

public final class Signatures {

	private static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();


	private Signatures()
	{}


	public static byte[] sign(byte[] contents, OpenedKeyStore keyStore, String signatureAlgorithm)
	{
		try {
			CMSSignedDataGenerator gen = createSignedDataGenerator(keyStore, signatureAlgorithm);

			CMSTypedData cmsData = new CMSProcessableByteArray(contents);
			CMSSignedData signedData = gen.generate(cmsData);
			return signedData.getEncoded();
		} catch(CMSException | OperatorException | IllegalArgumentException | IOException | GeneralSecurityException e) {
			throw new UncheckedSecurityException(new GeneralSecurityException(e));
		}
	}


	private static CMSSignedDataGenerator createSignedDataGenerator(OpenedKeyStore openedKeyStore, String signatureAlgorithm)
			throws GeneralSecurityException, OperatorException, CMSException
	{
		List<Certificate> certChain = Arrays.asList(openedKeyStore.getCertificateChain());
		JcaCertStore certStore = new JcaCertStore(certChain);
		Certificate cert = openedKeyStore.getCertificate();
		ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm).setProvider(PROVIDER).build(openedKeyStore.privateKey());
		CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
		DigestCalculatorProvider dcp = new JcaDigestCalculatorProviderBuilder().setProvider(PROVIDER).build();
		SignerInfoGenerator sig = new JcaSignerInfoGeneratorBuilder(dcp).build(signer, (X509Certificate) cert);
		generator.addSignerInfoGenerator(sig);
		generator.addCertificates(certStore);
		return generator;
	}


	public static List<X509Certificate> certificatesFromSignature(byte[] encapSigData)
	{
		try {
			CMSSignedDataParser parser = new CMSSignedDataParser(new JcaDigestCalculatorProviderBuilder().setProvider(PROVIDER).build(), encapSigData);

			@SuppressWarnings("unchecked")
			Store<X509CertificateHolder> certStore = parser.getCertificates();
			SignerInformationStore signers = parser.getSignerInfos();

			List<X509Certificate> certificates = new ArrayList<>();

			JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(PROVIDER);

			for(SignerInformation signer : signers.getSigners()) {
				@SuppressWarnings("unchecked")
				Collection<X509CertificateHolder> holders = certStore.getMatches(signer.getSID());

				for(X509CertificateHolder holder : holders) {
					X509Certificate certificate = Exceptional.apply(converter::getCertificate, holder);
					certificates.add(certificate);
				}
			}
			return certificates;
		} catch(CMSException | OperatorCreationException e) {
			throw new UncheckedSecurityException(new GeneralSecurityException(e));
		}
	}

}
