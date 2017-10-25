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

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

public class UserAgent implements ClientRequestFilter {

	static final String HEADER_USER_AGENT = "User-Agent";
	private final String value;


	public UserAgent(String userAgent)
	{
		this.value = userAgent;
	}


	@Override
	public void filter(ClientRequestContext requestContext) throws IOException
	{
		MultivaluedMap<String, Object> headers = requestContext.getHeaders();
		headers.add(HEADER_USER_AGENT, value);
	}
}
