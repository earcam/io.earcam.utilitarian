/*-
 * #%L
 * io.earcam.utilitarian.web
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

import static io.earcam.utilitarian.net.FreePortFinder.findFreePort;
import static io.earcam.utilitarian.web.jaxrs.BasicAuthenticator.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import com.acme.Echo;
import com.acme.EchoService;

//Note: currently `JerseyTest` requires vintaged JUnit4 - e.g. https://github.com/eclipse-ee4j/jersey/issues/3662,
public class JaxRsTest extends JerseyTest {

	private static final String USER_AGENT = "Mozilla/5.0 (X11; YouNix; Linux x86_64; rv:53.0) earcam.io/1.0";
	private static final String TOKEN = "0123456789abcdef";

	@Provider
	public static class BorkWithoutHeaders implements ContainerRequestFilter {

		@Override
		public void filter(ContainerRequestContext context) throws IOException
		{
			String tokenAuth = context.getHeaderString(AUTHORIZATION);
			if(!tokenAuth.equals("Bearer " + TOKEN)) {
				throw new IOException("No token auth header found with correct value: " + tokenAuth);
			}
			String userAgent = context.getHeaderString(UserAgent.HEADER_USER_AGENT);
			if(!USER_AGENT.equals(userAgent)) {
				throw new IOException("No valid user agent header found: " + userAgent);
			}
		}
	}


	@Override
	protected Application configure()
	{
		forceEnable(TestProperties.LOG_TRAFFIC);
		set(TestProperties.CONTAINER_PORT, findFreePort());

		ResourceConfig app = new ResourceConfig(EchoService.class);
		app.register(JsonMessageBodyReader.class);
		app.register(JsonMessageBodyWriter.class);
		app.register(new BorkWithoutHeaders());
		return app;
	}


	@Override
	protected void configureClient(ClientConfig config)
	{
		super.configureClient(config);

		config.register(JsonMessageBodyReader.class);
		config.register(JsonMessageBodyWriter.class);
		config.register(new TokenBearerAuthenticator(TOKEN));
		config.register(new UserAgent(USER_AGENT));
	}


	@org.junit.Test
	public void get()
	{
		Echo response = target("/echo/hello")
				.request()
				.accept(APPLICATION_JSON_TYPE)
				.get(Echo.class);
		assertThat("hello hello", is(equalTo(response.getEchoed())));
	}


	@org.junit.Test
	public void getAgain()
	{
		Echo response = target("echo/QUACK")
				.request()
				.accept(APPLICATION_JSON_TYPE)
				.get(Echo.class);
		assertThat("QUACK", is(equalTo(response.getEchoed())));
	}


	@org.junit.Test
	public void send()
	{
		Echo echo = new Echo();
		echo.setEchoed("location");

		Echo response = target("echo/echo")
				.request()
				.accept(APPLICATION_JSON_TYPE)
				.post(Entity.entity(echo, APPLICATION_JSON), Echo.class);

		assertThat("location location", is(equalTo(response.getEchoed())));
	}
}
