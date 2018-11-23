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

import static org.junit.jupiter.api.Assertions.*;

import java.security.cert.CertificateException;

import org.junit.jupiter.api.Test;

public class NoopTrustManagerTest {

	@Test
	public void clientsAreNeverTrusted()
	{
		try {
			new NoopTrustManager(true).checkClientTrusted(null, null);
			fail();
		} catch(CertificateException e) {}
	}


	@Test
	public void clientAreAlwaysTrusted() throws CertificateException
	{
		new NoopTrustManager(false).checkClientTrusted(null, null);
	}


	@Test
	public void serverNeverTrusted()
	{
		try {
			new NoopTrustManager(true).checkServerTrusted(null, null);
			fail();
		} catch(CertificateException e) {}
	}
}
