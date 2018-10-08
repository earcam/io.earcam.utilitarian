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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStore.LoadStoreParameter;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import io.earcam.unexceptional.Exceptional;

public class KeyStores {

	private KeyStores()
	{}


	public static KeyPair keyPair(KeyStore store, String alias, char[] password)
	{
		PrivateKey key = PrivateKey.class.cast(Exceptional.apply(store::getKey, alias, password));
		Certificate certificate = Exceptional.apply(store::getCertificate, alias);
		return new KeyPair(certificate.getPublicKey(), key);
	}


	public static KeyStore keyStore(KeyPairCredential credential, Certificate... certificates)
	{
		return keyStore(credential.name(), credential.password(), credential.pair(), certificates);
	}


	/**
	 * 
	 * @param alias key alias
	 * @param aliasPassword password
	 * @param pair the key pair to use
	 * @param certificates
	 * @return a KeyStore built from the supplied arguments
	 * @throws io.earcam.unexceptional.UncheckedSecurityException if {@link java.security.KeyStoreException},
	 * {@link java.security.NoSuchAlgorithmException} or {@link java.security.cert.CertificateException} is raised
	 * @throws java.io.UncheckedIOException if java.io.IOException is raised
	 */
	public static KeyStore keyStore(String alias, char[] aliasPassword, KeyPair pair, Certificate... certificates)
	{
		return keyStore("JKS", alias, aliasPassword, pair, certificates);
	}


	/**
	 * 
	 * @param type
	 * @param alias key alias
	 * @param aliasPassword password
	 * @param pair the key pair to use
	 * @param certificates
	 * @return a KeyStore built from the supplied arguments
	 * @throws io.earcam.unexceptional.UncheckedSecurityException if {@link java.security.KeyStoreException},
	 * {@link java.security.NoSuchAlgorithmException} or {@link java.security.cert.CertificateException} is raised
	 * @throws java.io.UncheckedIOException if java.io.IOException is raised
	 */
	public static KeyStore keyStore(String type, String alias, char[] aliasPassword, KeyPair pair, Certificate... certificates)
	{
		KeyStore keyStore = Exceptional.apply(KeyStore::getInstance, type);
		Exceptional.accept(keyStore::load, (LoadStoreParameter) null);
		Exceptional.run(() ->
		// TODO check - if there are no certificates then we should not provide the private key but the public?
		keyStore.setKeyEntry(alias, pair.getPrivate(), aliasPassword, certificates));
		return keyStore;
	}


	/**
	 * 
	 * @param keyStore
	 * @param password
	 * @return
	 * @throws io.earcam.unexceptional.UncheckedSecurityException if {@link java.security.KeyStoreException},
	 * {@link java.security.NoSuchAlgorithmException} or {@link java.security.cert.CertificateException} is raised
	 * @throws java.io.UncheckedIOException if java.io.IOException is raised
	 */
	public static byte[] encode(KeyStore keyStore, char[] password)
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Exceptional.accept(keyStore::store, output, password);
		return output.toByteArray();
	}


	/**
	 * 
	 * @param dehydrated
	 * @param password
	 * @return
	 * @throws io.earcam.unexceptional.UncheckedSecurityException if {@link java.security.KeyStoreException},
	 * {@link java.security.NoSuchAlgorithmException} or {@link java.security.cert.CertificateException} is raised
	 * @throws java.io.UncheckedIOException if java.io.IOException is raised
	 */
	public static KeyStore decode(byte[] dehydrated, char[] password)
	{
		return decode("JKS", dehydrated, password);
	}


	/**
	 * 
	 * @param type
	 * @param dehydrated
	 * @param password
	 * @return
	 * @throws io.earcam.unexceptional.UncheckedSecurityException if {@link java.security.KeyStoreException},
	 * {@link java.security.NoSuchAlgorithmException} or {@link java.security.cert.CertificateException} is raised
	 * @throws java.io.UncheckedIOException if java.io.IOException is raised
	 */
	public static KeyStore decode(String type, byte[] dehydrated, char[] password)
	{
		KeyStore rehydrated = Exceptional.apply(KeyStore::getInstance, type);
		Exceptional.accept(rehydrated::load, new ByteArrayInputStream(dehydrated), password);
		return rehydrated;
	}
}
