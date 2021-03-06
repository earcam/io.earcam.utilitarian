/*-
 * #%L
 * io.earcam.utilitarian.site.sitemap
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
package io.earcam.utilitarian.site.sitemap;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.earcam.unexceptional.Exceptional;
import io.earcam.utilitarian.net.FreePortFinder;

@SuppressWarnings("restriction")
public class SitemapSubmissionTest {

	@Test
	public void submit() throws IOException
	{
		int port = FreePortFinder.findFreePort();

		List<String> submittedPaths = new ArrayList<>();

		HttpServer server = createHttpServer(port, submittedPaths);
		server.start();

		String host1 = "http://localhost:" + port;
		String host2 = "http://127.0.0.1:" + port;

		URI base = Exceptional.uri("https://domain.acme.com/module/");
		Path targetDir = Paths.get("ficticious", "path");
		Stream<Path> sitemaps = Arrays.asList(targetDir.resolve(Paths.get("dir", "sitemap.xml"))).stream();

		String response = SitemapSubmission.submit(Arrays.asList(host1, host2), base, targetDir, sitemaps);

		String pathAndStatus = "/ping?sitemap=https%3A%2F%2Fdomain.acme.com%2Fmodule%2Fdir%2Fsitemap.xml - 200: OK,  ";

		assertThat(response, is(equalToIgnoringWhiteSpace(
				host1 + pathAndStatus +
						lineSeparator() +
						host2 + pathAndStatus)));

		server.stop(0);
	}


	static HttpServer createHttpServer(int port, List<String> submittedPaths) throws IOException
	{
		HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), port);
		HttpContext context = server.createContext("/");
		context.setHandler(new HttpHandler() {

			@Override
			public void handle(HttpExchange exchange) throws IOException
			{
				String pfft = exchange.getProtocol() + "://" + exchange.getLocalAddress().getHostString() + ':'
						+ exchange.getLocalAddress().getPort() + exchange.getRequestURI().getPath();

				submittedPaths.add(pfft);
				drain(exchange.getRequestBody());

				exchange.sendResponseHeaders(200, 0);
				exchange.getResponseBody().close();
			}


			private void drain(InputStream input) throws IOException
			{
				while(input.read() != -1);
			}
		});
		return server;
	}
}
