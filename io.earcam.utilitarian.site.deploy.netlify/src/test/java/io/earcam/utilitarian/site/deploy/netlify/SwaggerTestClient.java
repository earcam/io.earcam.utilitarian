/*-
 * #%L
 * io.earcam.utilitarian.site.deploy.netlify
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
package io.earcam.utilitarian.site.deploy.netlify;

import static javax.ws.rs.client.ClientBuilder.newBuilder;

import javax.ws.rs.client.Client;

import io.earcam.utilitarian.net.ssl.DummySslContext;

public class SwaggerTestClient {

	private static final Client JAX_RS_CLIENT = createJaxRsTestClient();


	public static ApiClient swaggerApiTestClient(String baseUrl)
	{
		return SwaggerClient.swaggerClient(baseUrl, JAX_RS_CLIENT);
	}


	public static Client createJaxRsTestClient()
	{
		return newBuilder()
				.hostnameVerifier(DummySslContext.dummyHostnameVerifier())
				.sslContext(DummySslContext.unverifiedClientSslContext())
				.register(SwaggerClient.JacksonObjectMapper.class)
				.build();
	}
}
