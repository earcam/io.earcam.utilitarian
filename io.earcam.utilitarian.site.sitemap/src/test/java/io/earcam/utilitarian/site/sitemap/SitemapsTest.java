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

import static io.earcam.unexceptional.Exceptional.uri;
import static java.nio.file.Files.readAllLines;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.io.FileMatchers.aFileWithSize;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import io.earcam.utilitarian.net.FreePortFinder;

@SuppressWarnings("restriction")
public class SitemapsTest {

	private static final Path SITE_BASE_DIR = Paths.get("src", "test", "resources", "dummy-site");
	private static final String BASE_URL = "http://example.com/";


	@Test
	public void createSitemap() throws Exception
	{
		Path targetPath = Paths.get("target", "Sitemaps.createSitemap", UUID.randomUUID().toString());
		Path listing = sitemap(targetPath);

		Path file = targetPath.resolve("sitemap1.xml").toAbsolutePath();

		assertThat(readAllLines(listing), contains(file.toString()));

		assertThat(file.toFile(), is(aFileWithSize(greaterThan(0L))));
	}


	private Path sitemap(Path targetPath) throws URISyntaxException
	{
		SitemapParameters parameters = new SitemapParameters(
				new URI(BASE_URL),
				SITE_BASE_DIR.resolve(Paths.get("directory%b", "directory-ba")),
				targetPath);

		targetPath.toFile().mkdirs(); // TODO move into Sitemap code

		Path listing = Sitemaps.create(parameters);
		return listing;
	}


	@Test
	public void index() throws Exception
	{
		Path targetPath = Paths.get("target", "Sitemaps.index", UUID.randomUUID().toString());
		sitemap(targetPath);

		Path indicesListing = Sitemaps.index(targetPath, Stream.of(targetPath));

		Path file = targetPath.resolve("sitemapindex1.xml").toAbsolutePath();

		assertThat(readAllLines(indicesListing), contains(file.toString()));

		assertThat(file.toFile(), is(aFileWithSize(greaterThan(0L))));
	}


	@Test
	public void robotsTxt() throws Exception
	{
		Path targetPath = Paths.get("target", "Sitemaps.index", UUID.randomUUID().toString());
		sitemap(targetPath);
		Sitemaps.index(targetPath, Stream.of(targetPath));

		Sitemaps.robotsTxt(targetPath);

		String robotsTxt = readAllLines(targetPath.resolve("robots.txt"))
				.stream()
				.collect(joining("\n"));

		assertThat(robotsTxt, is(equalToIgnoringWhiteSpace(
				// @formatter:off
				"User-agent: *             \n" +
				"Allow: /                  \n" +
				"Disallow:                 \n" +
				"sitemap: ./sitemapindex1.xml")));
				// @formatter:on
	}


	@Test
	public void submission() throws Exception
	{
		int port = FreePortFinder.findFreePort();

		HttpServer httpServer = SitemapSubmissionTest.createHttpServer(port, new ArrayList<>());
		httpServer.start();

		Path targetPath = Paths.get("target", "Sitemaps.submit", UUID.randomUUID().toString());
		sitemap(targetPath);

		Sitemaps.index(targetPath, Stream.of(targetPath));

		String submitted = Sitemaps.submit(targetPath, uri("http://web.acme.com/foo/bar/hum/bug/"), singletonList("http://localhost:" + port));

		httpServer.stop(0);

		assertThat(submitted, is(equalToIgnoringWhiteSpace(
				"http://localhost:" + port + "/ping?sitemap=http%3A%2F%2Fweb.acme.com%2Ffoo%2Fbar%2Fhum%2Fbug%2Fsitemapindex1.xml - 200: OK,")));
	}
}
