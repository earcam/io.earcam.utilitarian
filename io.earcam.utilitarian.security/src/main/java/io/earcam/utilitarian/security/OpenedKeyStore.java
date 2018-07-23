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

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Objects;

import javax.security.auth.Destroyable;

import io.earcam.unexceptional.Exceptional;

public class OpenedKeyStore implements Destroyable, AutoCloseable {

	private KeyStore store;
	private KeyPairCredential credential;


	public OpenedKeyStore(KeyStore store, KeyPairCredential credential)
	{
		Objects.requireNonNull(store, "store");
		this.store = store;
		this.credential = credential;
	}


	public OpenedKeyStore(KeyStore store, String alias, char[] password)
	{
		this(store, new KeyPairCredential(KeyStores.keyPair(store, alias, password), alias, password));
	}


	@Override
	public void close()
	{
		destroy();
	}


	@Override
	public void destroy()
	{
		store = null;
		credential.destroy();
	}


	@Override
	public boolean isDestroyed()
	{
		return store == null;
	}


	public KeyStore store()
	{
		return store;
	}


	public PublicKey publicKey()
	{
		return credential.publicKey();
	}


	public PrivateKey privateKey()
	{
		return credential.privateKey();
	}


	public Certificate[] getCertificateChain()
	{
		return Exceptional.apply(store::getCertificateChain, credential.name());
	}


	public Certificate getCertificate()
	{
		return Exceptional.apply(store::getCertificate, credential.name());
	}
}
