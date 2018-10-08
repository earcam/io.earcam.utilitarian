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

import static io.earcam.unexceptional.Exceptional.uncheckFunction;
import static io.earcam.utilitarian.site.sitemap.Jaxb.unmarshal;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.junit.Test;
import org.sitemaps.TUrl;
import org.sitemaps.Urlset;

/*

tests:
[X]	sitemap splitOnMaxSize (500, 400, 300, 251)
[ ]	splitOnMaxRecords // sitemap must have no more the 50k entries
[ ]	sitemapIndex splitOnMaxSize
[ ]	sitemapIndex splitOnMaxRecords

[ ] Do the refactoring to stream all with hand-rolled XML output, but use JAXB to test  (this way can move JAXB util back into test (later move out to own module with full tests))

 */
public class SitemapTest {

	private static final Path SITE_BASE_DIR = Paths.get("src", "test", "resources", "dummy-site");
	private static final String BASE_URL = "http://example.com/";


	@Test
	public void simpleSitemap() throws IOException, URISyntaxException
	{
		SitemapParameters parameters = new SitemapParameters(
				new URI(BASE_URL),
				SITE_BASE_DIR.resolve(Paths.get("directory%b", "directory-ba")),
				Paths.get("target", "simpleSitemap", UUID.randomUUID().toString()));

		parameters.targetDir.toFile().mkdirs(); // TODO move into Sitemap code

		List<Path> maps = new ArrayList<>();

		Sitemap sitemap = new Sitemap(parameters, maps::add);
		sitemap.run();

		assertThat(maps, hasSize(1));

		String xml = new String(Files.readAllBytes(maps.iterator().next()), UTF_8);
		Urlset urlset = Jaxb.unmarshal(xml, Urlset.class);
		List<String> earls = urlset.getUrl().stream().map(TUrl::getLoc).collect(toList());

		assertThat(earls, contains(parameters.base + "index.html"));
	}


	@Test
	public void siteMapFromDirectory() throws IOException, URISyntaxException
	{
		List<Path> maps = new ArrayList<>();
		Sitemap sitemap = sitemap(maps::add);

		sitemap.run();

		assertThat(maps, hasSize(1));

		String xml = new String(Files.readAllBytes(maps.iterator().next()), UTF_8);
		Urlset urlset = Jaxb.unmarshal(xml, Urlset.class);
		List<String> earls = urlset.getUrl().stream().map(TUrl::getLoc).collect(toList());

		assertThat(earls, containsInAnyOrder(
				BASE_URL + "directory%20a/directory-aa/index.html",
				BASE_URL + "directory%20a/directory-ab/duplicate-index.html",
				BASE_URL + "directory%20a/directory-ab/index.html",
				BASE_URL + "directory%25b/directory-ba/index.html",
				BASE_URL + "directory%25b/directory-bb/index.html",
				BASE_URL + "directory%25b/directory-bc/index.html"));
	}


	private Sitemap sitemap(Consumer<Path> generatedFileRecorder) throws URISyntaxException
	{
		SitemapParameters parameters = new SitemapParameters(
				new URI("http://example.com/"),
				SITE_BASE_DIR,
				Paths.get("target", "siteMapFromDirectory", UUID.randomUUID().toString()));
		parameters.options().setInclude(Pattern.compile(".*\\.html?$"));

		parameters.targetDir.toFile().mkdirs(); // FIXME move into Sitemap code

		Sitemap sitemap = new Sitemap(parameters, generatedFileRecorder);
		return sitemap;
	}


	@Test
	public void splitOnMaxSize500() throws IOException, URISyntaxException
	{
		splitOnMaxLength(500, 2);
	}


	private void splitOnMaxLength(int maxLength, int expectedNumberOfSplits) throws IOException, URISyntaxException
	{
		List<Path> maps = new ArrayList<>();
		Sitemap sitemap = sitemap(maps::add);
		sitemap.maxSize = maxLength;

		sitemap.run();

		long urls = maps.stream()
				.map(uncheckFunction(Files::readAllBytes))
				.map(b -> new String(b, UTF_8))
				.map(x -> unmarshal(x, Urlset.class))
				.map(Urlset::getUrl)
				.flatMap(List::stream)
				.count();

		assertThat(maps, hasSize(expectedNumberOfSplits));
		assertThat("all elements accounted for", urls, is(equalTo(6L)));

	}


	@Test
	public void splitOnMaxSize400() throws IOException, URISyntaxException
	{
		splitOnMaxLength(400, 4);
	}


	@Test
	public void splitOnMaxSize300() throws IOException, URISyntaxException
	{
		splitOnMaxLength(300, 6);
	}


	// Smallest record size in fixed data set - anything lower triggers buffer overflow
	@Test
	public void splitOnMaxSize254() throws IOException, URISyntaxException
	{
		splitOnMaxLength(254, 6);
	}
}
