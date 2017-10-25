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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import io.earcam.unexceptional.Exceptional;
import io.earcam.utilitarian.site.search.offline.ConfigurationModel.Crawling;
import io.earcam.utilitarian.site.search.offline.ConfigurationModel.Indexing;
import io.earcam.utilitarian.site.search.offline.jsonb.JsonBind;

public class Search {

	public static void main(String[] args)
	{
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
		if(args.length < 2) {
			throw new IllegalArgumentException(
					"Expected first arg to be json-output-dir, then at least a further argument of the form 'baseDir@baseUri'");
		}

		final String jsonDir = args[0];
		Indexer indexer = null;
		for(int i = 1; i < args.length; i++) {
			int index = args[i].indexOf('@');
			if(index == -1) {
				throw new IllegalArgumentException(
						"Only arguments expected are 'baseDir@baseUri', recieved '" +
								args[i] + "' at index " + i);
			}
			Path baseDir = Paths.get(args[i].substring(0, index));
			URI baseUri = Exceptional.uri(args[i].substring(index + 1));
			Map<String, String> searchReplace = searchReplaceMap(jsonDir, baseDir, baseUri);

			String crawlerJson = Resources.getResource(Resources.DEFAULT_CRAWLER_JSON, UTF_8, searchReplace);
			Crawling crawler = JsonBind.readJson(crawlerJson, Crawling.class);

			if(indexer == null) {
				String indexerJson = Resources.getResource(Resources.DEFAULT_INDEXER_JSON, UTF_8, searchReplace);
				indexer = JsonBind.readJson(indexerJson, Indexing.class).build();
			}
			indexer.add(crawler.build().documents());
		}
		// @SuppressWarnings("squid:S2259")
		indexer.writeJson();  //NOSONAR false positive
	}


	private static Map<String, String> searchReplaceMap(String jsonDir, Path baseDir, URI baseUri)
	{
		Map<String, String> searchReplace = new HashMap<>();
		searchReplace.put("${outputCharset}", UTF_8.name());
		searchReplace.put("${jsonDir}", jsonDir);
		searchReplace.put("${baseDir}", baseDir.toAbsolutePath().toString());
		searchReplace.put("${baseUri}", baseUri.toString());
		return searchReplace;
	}
}
