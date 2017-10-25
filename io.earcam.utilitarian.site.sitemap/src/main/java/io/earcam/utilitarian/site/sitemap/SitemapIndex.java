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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public final class SitemapIndex extends AbstractSitemap {

	public static final String NAME_SITEMAP_INDEX = "sitemapindex";

	private static final String TAG_SITEMAP = "sitemap";

	private static final String HEAD = "<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">";

	private static final String TAIL = "</sitemapindex>";

	private SitemapParameters currentParameters;

	private URI targetUri;


	public SitemapIndex(SitemapParameters parameters, Consumer<Path> generatedFileRecorder)
	{
		super(parameters, HEAD, TAG_SITEMAP, TAIL, generatedFileRecorder);
	}


	public void accept(Stream<Map.Entry<SitemapParameters, Stream<String>>> sitemaps) throws IOException
	{
		Stream<Path> paths = sitemaps.sequential()
				.flatMap(this::perGroup);

		super.process(paths);
	}


	private Stream<Path> perGroup(Map.Entry<SitemapParameters, Stream<String>> sitemaps)
	{
		this.currentParameters = sitemaps.getKey();
		this.targetUri = currentParameters.targetDir.toUri();

		return sitemaps.getValue().map(Paths::get);
	}


	@Override
	protected Path filename()
	{
		return filename(parameters, NAME_SITEMAP_INDEX + (++indexFileSuffix));
	}


	@Override
	protected String createUrl(Path sitemapXml) throws IOException
	{
		URI uri = targetUri.relativize(sitemapXml.toUri());
		return currentParameters.base.resolve(uri).toString();
	}
}
