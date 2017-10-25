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

import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

final class NoopTrustManager implements X509TrustManager {

	private static final TrustManager[] NOOP_TRUST_MANAGERS = new TrustManager[] { new NoopTrustManager() };


	private NoopTrustManager()
	{}


	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
	{
		/* NOOP */
	}


	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
	{
		/* NOOP */
	}


	@Override
	public X509Certificate[] getAcceptedIssuers()
	{
		return new X509Certificate[0];
	}


	public static TrustManager[] noopTrustManager()
	{
		return NOOP_TRUST_MANAGERS;
	}
}
