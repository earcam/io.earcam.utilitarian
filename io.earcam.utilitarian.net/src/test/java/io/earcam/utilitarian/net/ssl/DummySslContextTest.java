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
// EARCAM_SNIPPET_BEGIN: example
package io.earcam.utilitarian.net.ssl;

import static io.earcam.utilitarian.net.FreePortFinder.findFreePort;              // ⓘ
import static io.earcam.utilitarian.net.ssl.DummySslContext.serverSslContext;     // ⓘ
import static io.earcam.utilitarian.net.ssl.DummySslContext.unverifiedResponse;   // ⓘ
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.junit.Test;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

public class DummySslContextTest {

	final String serverResponse = "hello Untrustworthy, Unverified and Unknown World Wide Web";


	@Test
	public void clientAndServer() throws Exception
	{
		DummySslContext.enableSslDebug();  // ⓘ

		int port = findFreePort();  // ⓘ

		String hostname = "localhost";
		String path = "/meh";
		String earl = "https://" + hostname + ":" + port + path;

		HttpsServer server = createJdkHttpsServer(hostname, port);

		HttpContext context = server.createContext(path);
		context.setHandler(new HttpHandler() {
			@Override
			public void handle(HttpExchange exchange) throws IOException
			{
				byte[] content = serverResponse.getBytes(UTF_8);
				exchange.sendResponseHeaders(200, content.length);
				try(OutputStream responseBody = exchange.getResponseBody()) {
					responseBody.write(content);
				}
			}

		});

		server.start();

		byte[] response = unverifiedResponse(earl);  // ⓘ

		String responseBody = new String(response, UTF_8);
		assertThat(responseBody, is(equalTo(serverResponse)));

		server.stop(0);
	}


	public static HttpsServer createJdkHttpsServer(String hostname, int port) throws IOException
	{
		InetSocketAddress address = new InetSocketAddress(hostname, port);
		HttpsServer server = HttpsServer.create(address, address.getPort());
		server.setHttpsConfigurator(new HttpsConfigurator(serverSslContext(hostname)));  // ⓘ
		return server;
	}

}
// EARCAM_SNIPPET_END: example
