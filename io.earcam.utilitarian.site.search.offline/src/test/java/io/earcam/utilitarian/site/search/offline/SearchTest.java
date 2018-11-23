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

import static io.earcam.unexceptional.Exceptional.apply;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.earcam.unexceptional.Exceptional;

public class SearchTest {

	static {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
	}


	/**
	 * Checks the equivalence of {@link Search#main(String[])} against programmatically configured.
	 */
	@Test
	public void equivalence()
	{
		final Path baseDir = Paths.get("src", "test", "resources", "dummysite");
		final URI baseUri = Exceptional.uri("http://acme.earcam.io/dummy/");

		final String include = ".*";
		final String exclude = "^(.*dependenc.*|.*\\/jacoco\\-.*|.*\\/pit-reports\\/.*|.*\\/404.html)$";

		final Path outputDir = Paths.get(".", "target", UUID.randomUUID().toString());

		final String fields = Document.TEXT + ',' + Document.TITLE + ',' + Document.DESCRIPTION;

		final Path testJson = outputDir.resolve(Paths.get("programmatic.json"));
		final Path mainJson = outputDir.resolve(Paths.get("search-data.json"));

		// Programmatic ...

		Filter filter = new RegexFilter();
		Map<String, String> configuration = new HashMap<>();
		configuration.put(RegexFilter.INCLUDE, include);
		configuration.put(RegexFilter.EXCLUDE, exclude);
		filter.configure(configuration);

		Stream<Document> documents = Crawler.crawler(singletonMap(baseDir, baseUri))
				.filter(filter)
				.processor(new HtmlContentProcessor())
				.processor(new PdfContentProcessor())
				.processor(new SimpleTokenizer())
				.documents();

		DefaultIndexer indexer = new DefaultIndexer();
		configuration.clear();
		configuration.put(Document.REF_URL, Document.REF_URL);
		configuration.put(DefaultIndexer.OUTPUT_FILE, testJson.toString());
		configuration.put(DefaultIndexer.FIELDS, fields);
		indexer.configure(configuration);

		indexer.add(documents);
		indexer.writeJson();

		// ... versus Search.main(args)

		String[] args = new String[] {
				mainJson.getParent().toString(),
				dirUri(baseDir, baseUri),
		};

		Search.main(args);

		String test = new String(apply(Files::readAllBytes, testJson), UTF_8);
		String main = new String(apply(Files::readAllBytes, mainJson), UTF_8);

		assertThat(main, is(equalTo(test)));
	}


	private String dirUri(Path baseDir, URI baseUri)
	{
		return baseDir.toString() + '@' + baseUri;
	}
}
