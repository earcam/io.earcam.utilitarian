/*-
 * #%L
 * io.earcam.utilitarian.site.search.offline
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
package io.earcam.utilitarian.site.search.offline;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.earcam.unexceptional.EmeticStream;

// @formatter:off
/**
 * Pipeline needs to be built according to definition, with names driven via SPI, i.e.
 *
 * configuration is just a Map<String, String> passed to each
 *
 * <pre>
 * &lt;pipeline&gt;
 *    &lt;filter&gt;
 *    	 &lt;id&gt;default-regex&lt;/id&gt;
 *    	 &lt;configuration&gt;
 *          &lt;include&gt;regex&lt;/include&gt;
 *          &lt;exclude&gt;regex&lt;/exclude&gt;
 *       &lt;/configuration&gt;
 *    &lt;/filter&gt;
 *    &lt;processor&gt;
 *       &lt;id&gt;default-html&lt;/id&gt;
 *    &lt;/processor&gt;
 *    &lt;processor&gt;
 *       &lt;id&gt;default-pdf&lt;/id&gt;
 *    &lt;/processor&gt;
 *    &lt;!-- ... filter based on content can go here ... --&gt;
 *    &lt;processor&gt;
 *       &lt;id&gt;default-tokenizer&lt;/id&gt;
 *    &lt;/processor&gt;
 * &lt;/pipeline&gt;
 * </pre>
 *
 * Therefore Filter and Processor both need to extend 'Component'
 *
 * Component{ String id; void configure(Map<String, String>) }
 *
 * Filter imps Predicate<Document>
 *
 * Processor{ process(Document); }
 *
 *
 * HtmlContentParser{ }
 *
 *
 */
// @formatter:on
public class Crawler {

	private Stream<Document> documents;


	public static Crawler crawler(Map<Path, URI> directories)
	{
		Crawler crawler = new Crawler();
		crawler.documents = crawl(directories);
		return crawler;
	}


	private static Stream<Document> crawl(Map<Path, URI> directories)
	{
		return directories.entrySet().parallelStream().flatMap(Crawler::crawl);
	}


	private static Stream<Document> crawl(Map.Entry<Path, URI> pair)
	{
		return crawl(pair.getKey(), pair.getValue());
	}


	private static Stream<Document> crawl(Path baseDir, URI baseUri)
	{
		return EmeticStream.emesis(Files::walk, baseDir)
				.mapToStream()
				.filter(p -> p.toFile().isFile())
				.map(f -> Document.document(baseDir, baseUri, f));
	}


	public Crawler filter(Predicate<Document> filter)
	{
		documents = documents.filter(filter);
		return this;
	}


	public Crawler processor(Processor processor)
	{
		documents = documents.map(processor);
		return this;
	}


	public Stream<Document> documents()
	{
		return documents.filter(Document::hasTokens);
	}
}
