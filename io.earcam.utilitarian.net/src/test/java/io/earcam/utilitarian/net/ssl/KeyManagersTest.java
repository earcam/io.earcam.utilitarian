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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509ExtendedKeyManager;

import org.junit.Test;

import io.earcam.utilitarian.net.ssl.KeyManagers.DummyX509KeyManager;

public class KeyManagersTest {

	@SuppressWarnings("unchecked")
	@Test
	public void onlyOneDummyKeyManager()
	{
		KeyManager[] dummies = KeyManagers.keyManagerDummy();

		assertThat(dummies, is(arrayContaining(instanceOf(DummyX509KeyManager.class))));
	}


	@Test
	public void dummyX509KeyManagerDoesNothing()
	{
		X509ExtendedKeyManager dummy = KeyManagers.NOOP_KEY_MANAGER;

		assertThat(dummy.getClientAliases(null, null), is(emptyArray()));
		assertThat(dummy.getServerAliases(null, null), is(emptyArray()));

		assertThat(dummy.chooseClientAlias(null, null, null), is(nullValue()));
		assertThat(dummy.chooseServerAlias(null, null, null), is(nullValue()));

		assertThat(dummy.getCertificateChain(null), is(emptyArray()));
		assertThat(dummy.getPrivateKey(null), is(nullValue()));
	}

}
