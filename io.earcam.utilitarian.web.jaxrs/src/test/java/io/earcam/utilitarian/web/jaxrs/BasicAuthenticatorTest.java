/*-
 * #%L
 * io.earcam.utilitarian.web.jaxrs
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
package io.earcam.utilitarian.web.jaxrs;

import static io.earcam.utilitarian.web.jaxrs.BasicAuthenticator.AUTHORIZATION;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.jupiter.api.Test;

public class BasicAuthenticatorTest {

	@Test
	public void happy() throws Exception
	{
		BasicAuthenticator authenticator = new BasicAuthenticator("username", "password");

		MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
		ClientRequestContext request = mock(ClientRequestContext.class);
		given(request.getHeaders()).willReturn(headers);

		authenticator.filter(request);

		assertThat(headers, hasEntry(AUTHORIZATION, asList("BASIC dXNlcm5hbWU6cGFzc3dvcmQ=")));
	}
}
