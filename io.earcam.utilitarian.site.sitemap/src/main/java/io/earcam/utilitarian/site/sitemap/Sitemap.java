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

import static io.earcam.utilitarian.site.sitemap.SitemapParameters.INCLUDE_ALL;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class Sitemap extends AbstractSitemap {

	public static final String NAME_SITEMAP = "sitemap";

	private static final String TAG_URL = "url";

	private static final String HEAD = "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">";

	private static final String TAIL = "</urlset>";

	URI sourceUri;


	public Sitemap(SitemapParameters parameters, Consumer<Path> generatedFileRecorder)
	{
		super(parameters, HEAD, TAG_URL, TAIL, generatedFileRecorder);
		sourceUri = parameters.sourceDir.toUri();
	}


	public void run() throws IOException
	{
		Stream<Path> files = Files.walk(parameters.sourceDir)
				.filter(Files::isRegularFile);
		files = filter(files);
		process(files.sequential());
	}


	private Stream<Path> filter(Stream<Path> files)
	{
		if(!INCLUDE_ALL.equals(parameters.options().include())) {
			Predicate<String> filter = parameters.options().include().asPredicate();
			return files.filter(f -> filter.test(f.toAbsolutePath().toString()));
		}
		return files;
	}


	@Override
	protected Path filename()
	{
		return filename(parameters, NAME_SITEMAP + (++indexFileSuffix));
	}


	/*
	 * Relativize the local dir as URI then apply as resolve to base URL
	 */
	@Override
	protected String createUrl(Path siteResourceFile) throws IOException
	{
		URI uri = sourceUri.relativize(siteResourceFile.toUri());
		return parameters.base.resolve(uri).toString();
	}
}
