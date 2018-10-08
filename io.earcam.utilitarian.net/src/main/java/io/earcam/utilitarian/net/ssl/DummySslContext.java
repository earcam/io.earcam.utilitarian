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

import static io.earcam.utilitarian.net.ssl.KeyManagers.keyManagerDummy;
import static io.earcam.utilitarian.net.ssl.KeyManagers.keyManagerSunX509;
import static io.earcam.utilitarian.net.ssl.NoopTrustManager.noopTrustManager;
import static io.earcam.utilitarian.security.KeyStores.keyStore;
import static io.earcam.utilitarian.security.Keys.rsa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import io.earcam.unexceptional.Exceptional;
import io.earcam.unexceptional.UncheckedSecurityException;
import io.earcam.utilitarian.security.Certificates;

/**
 * <h1>Use for testing <b>only</b></h1> - otherwise why bother? Be honest about in/security
 */
public final class DummySslContext {

	private static final String PROTOCOL_SSL_V3 = "SSLv3";

	private static final class DummyHostnameVerifier implements HostnameVerifier {

		private final boolean always;


		DummyHostnameVerifier(boolean always)
		{
			this.always = always;
		}


		@Override
		public boolean verify(String hostname, SSLSession session)
		{
			return always;
		}

	}

	public static final HostnameVerifier ALWAYS_PASS_HOSTNAME_VERIFIER = new DummyHostnameVerifier(true);
	public static final HostnameVerifier ALWAYS_FAIL_HOSTNAME_VERIFIER = new DummyHostnameVerifier(true);


	private DummySslContext()
	{}


	/**
	 * Generates a permissive {@link SSLContext} with generated (in-memory) self-signed X509Certificate
	 *
	 * <p>
	 * <b>USE FOR TESTING ONLY</b>
	 * </p>
	 *
	 * @param host The hostname/IP
	 * @return a configured SSLContext
	 * @throws UncheckedSecurityException
	 * @throws UncheckedIOException
	 */
	public static SSLContext serverSslContext(String host)
	{
		return Exceptional.apply(DummySslContext::serverSslContext, host, new char[] {'p', 'a', 's', 't', 'W', 'e', 'i', 'r', 'd'});
	}


	/**
	 * Generates a permissive {@link SSLContext} with generated (in-memory) self-signed X509Certificate
	 *
	 * <p>
	 * <b>USE FOR TESTING ONLY</b>
	 * </p>
	 *
	 * @param host The hostname/IP
	 * @param password the password to use for both keystore and certificate alias
	 * @return a configured SSLContext
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static SSLContext serverSslContext(String host, char[] password) throws GeneralSecurityException
	{
		KeyPair pair = rsa();
		X509Certificate x509 = Certificates.certificate(pair, "DN=" + host + ", L=London, C=GB").toX509();
		KeyStore keyStore = keyStore("alias", password, pair, x509);

		SSLContext sslContext = SSLContext.getInstance(PROTOCOL_SSL_V3);
		sslContext.init(keyManagerSunX509(keyStore, password), noopTrustManager(), new SecureRandom());
		return sslContext;
	}


	/**
	 *
	 * @return a permissive (SSLv3 supporting TLSv1) SSLContext for client use
	 *
	 * @see #unverifiedConnection(String)
	 * @see #unverifiedConnection(URL)
	 */
	public static SSLContext unverifiedClientSslContext()
	{
		return Exceptional.apply(DummySslContext::unverifiedClientSslContext, PROTOCOL_SSL_V3);
	}


	public static SSLContext unverifiedClientSslContext(String sslProtocol) throws NoSuchAlgorithmException, KeyManagementException
	{
		SSLContext context = SSLContext.getInstance(sslProtocol);
		initialiseUnverified(context);
		return context;
	}


	private static void initialiseUnverified(SSLContext context) throws KeyManagementException
	{
		context.init(keyManagerDummy(), noopTrustManager(), new SecureRandom());
	}


	public static HostnameVerifier dummyHostnameVerifier()
	{
		return ALWAYS_PASS_HOSTNAME_VERIFIER;
	}


	public static HttpsURLConnection unverifiedConnection(String httpsUrl)
	{
		URL url = Exceptional.url(httpsUrl);
		return Exceptional.apply(DummySslContext::unverifiedConnection, url);
	}


	public static HttpsURLConnection unverifiedConnection(URL httpsUrl) throws IOException
	{
		HttpsURLConnection connection = (HttpsURLConnection) httpsUrl.openConnection();
		connection.setHostnameVerifier(dummyHostnameVerifier());
		connection.setSSLSocketFactory(unverifiedClientSslContext().getSocketFactory());
		return connection;
	}


	public static byte[] unverifiedResponse(String httpsUrl) throws IOException
	{
		HttpsURLConnection connection = unverifiedConnection(httpsUrl);
		connection.connect();
		InputStream input = connection.getInputStream();

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int b;
		while((b = input.read()) != -1) {
			output.write(b);
		}
		return output.toByteArray();
	}


	public static void enableSslDebug()
	{
		System.setProperty("javax.net.debug", "ssl:all");
	}
}
