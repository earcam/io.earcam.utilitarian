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

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

/**
 * <p>
 * A JAX-RS client filter providing support for the
 * <a href="https://en.wikipedia.org/wiki/OAuth">OAuth/Token Bearer</a>
 * form of <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP Authentication</a>
 * scheme.
 * </p>
 *
 * <p>
 * <b>Note</b>: unfortunately implementations (e.g. Jersey) don't acknowledge the risks
 * of interned String passwords by allowing the header value to be set as a {@code char} array.
 * This is likely due to API design, e.g. {@link javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate}
 * </p>
 */
public class TokenBearerAuthenticator implements ClientRequestFilter {

	private static final String TYPE_PREFIX = "Bearer ";
	private final String token;


	public TokenBearerAuthenticator(String token)
	{
		this.token = token;
	}


	@Override
	public void filter(ClientRequestContext requestContext) throws IOException
	{
		MultivaluedMap<String, Object> headers = requestContext.getHeaders();
		headers.add(AUTHORIZATION, TYPE_PREFIX + token);
	}
}
