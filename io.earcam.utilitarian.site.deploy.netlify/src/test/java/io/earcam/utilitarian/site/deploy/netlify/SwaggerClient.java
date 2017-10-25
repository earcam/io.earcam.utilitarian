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

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import javax.ws.rs.client.Client;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class SwaggerClient {

	private SwaggerClient()
	{}


	public static ApiClient swaggerClient(String baseUrl, Client client)
	{
		ApiClient apiClient = new ApiClient();
		apiClient.setHttpClient(client);
		//		apiClient.setUserAgent("Mozilla/5.0 (X11; YouNix; Linux x86_64; rv:53.0) earcam.io/1.0"); //TODO will Netlify accept this?
		apiClient.setBasePath(baseUrl);
		return apiClient;
	}

	@Provider
	protected static class JacksonObjectMapper implements ContextResolver<ObjectMapper> {
		final ObjectMapper defaultObjectMapper;


		public JacksonObjectMapper()
		{
			defaultObjectMapper = createDefaultMapper();
			defaultObjectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
		}


		@Override
		public ObjectMapper getContext(Class<?> type)
		{
			return defaultObjectMapper;
		}


		private static ObjectMapper createDefaultMapper()
		{
			final ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			return mapper;
		}
	}
}
