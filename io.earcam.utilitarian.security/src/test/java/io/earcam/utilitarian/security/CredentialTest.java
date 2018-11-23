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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

public class CredentialTest {

	@Test
	public void propertiesAreAvailable()
	{
		@SuppressWarnings("resource")
		Credential credential = new Credential("name", "password".toCharArray());

		assertThat(credential.isDestroyed(), is(false));
		assertThat(credential.name(), is(equalTo("name")));
		assertThat(credential.password(), is("password".toCharArray()));
	}


	@Test
	public void destroy()
	{
		@SuppressWarnings("resource")
		Credential credential = new Credential("name", "password".toCharArray());

		credential.destroy();

		assertThat(credential.isDestroyed(), is(true));
		assertThat(credential.name(), is(nullValue()));
		assertThat(credential.password(), is(nullValue()));
	}


	@Test
	public void closingDestroys()
	{
		Credential credential = new Credential("name", "password".toCharArray());

		credential.close();

		assertThat(credential.isDestroyed(), is(true));
		assertThat(credential.name(), is(nullValue()));
		assertThat(credential.password(), is(nullValue()));
	}
}
