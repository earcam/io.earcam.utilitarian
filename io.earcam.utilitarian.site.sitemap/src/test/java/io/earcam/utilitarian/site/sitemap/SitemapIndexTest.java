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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.sitemaps.index.Sitemapindex;
import org.sitemaps.index.TSitemap;

import io.earcam.unexceptional.Exceptional;
import io.earcam.utilitarian.site.sitemap.SitemapIndex;
import io.earcam.utilitarian.site.sitemap.SitemapParameters;

public class SitemapIndexTest {

	private static final String BASE_URL = "http://rock.kasbah.com/shareef/likes";


	@Test
	public void simpleSitemapIndex() throws IOException, URISyntaxException
	{
		Path fakeBaseDir = Paths.get("target", "simpleSitemapIndex", UUID.randomUUID().toString());

		SitemapParameters parameters = new SitemapParameters(new URI(BASE_URL), fakeBaseDir.resolve("sauce"), fakeBaseDir.resolve("targit"));

		List<Path> indices = new ArrayList<>();

		SitemapIndex index = new SitemapIndex(parameters, indices::add);

		Path relativeSitemap = Paths.get("not", "the", "sitemap.xml");
		Path sitemap = parameters.targetDir.resolve(relativeSitemap);

		touch(sitemap);

		Stream<String> sitemaps = Stream.of(sitemap).map(Object::toString);

		index.accept(Stream.of(new AbstractMap.SimpleEntry<SitemapParameters, Stream<String>>(parameters, sitemaps)));

		assertThat(indices, hasSize(1));

		List<String> earls = readSitemapUrlsFromFiles(indices.get(0));

		assertThat(earls, contains(BASE_URL + '/' + relativeSitemap));
	}


	private List<String> readSitemapUrlsFromFiles(Path index) throws IOException
	{
		List<String> earls = Jaxb.unmarshal(new String(Files.readAllBytes(index), UTF_8), Sitemapindex.class)
				.getSitemap()
				.stream()
				.map(TSitemap::getLoc)
				.collect(toList());
		return earls;
	}


	private static void touch(Path file)
	{
		if(!file.getParent().toFile().exists()) {
			if(!file.getParent().toFile().mkdirs()) {
				throw new UncheckedIOException(new IOException("Failed to create directory: " + file.getParent()));
			}
		}
		if(file.toFile().isDirectory()) {
			throw new UncheckedIOException(new IOException("Cannot touch file, as it's a directory: " + file));
		}

		if(!file.toFile().exists()) {
			Exceptional.run(() -> {
				Files.write(file.toAbsolutePath(), new byte[0]);
			});
		}
	}
}
