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

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyPairCredential extends Credential {

	private KeyPair pair;


	public KeyPairCredential(KeyPair pair, String name, char[] password)
	{
		super(name, password);
		this.pair = pair;
	}


	@Override
	public void destroy()
	{
		super.destroy();
		pair = null;
	}


	public KeyPair pair()
	{
		return pair;
	}


	public PublicKey publicKey()
	{
		return pair().getPublic();
	}


	public PrivateKey privateKey()
	{
		return pair().getPrivate();
	}
}
