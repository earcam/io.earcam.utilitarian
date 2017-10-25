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
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

public class ResourcesTest {

	@Test
	public void propertyReplace()
	{
		Map<String, String> searchReplace = Collections.singletonMap("${jsonDir}", "HUMBUG");
		String json = Resources.getResource(Resources.DEFAULT_INDEXER_JSON, UTF_8, searchReplace);

		assertThat(json, is(equalToIgnoringWhiteSpace(
				// @formatter:off
				"{                                                   \n" +
				"	\"id\": \"DefaultIndexer\",                      \n" +
				"	\"configuration\": {                             \n" +
				"		\"url\": \"url\",                            \n" +
				"		\"outputFile\": \"HUMBUG/search-data.json\", \n" +
				"		\"outputCharset\": \"${outputCharset}\",     \n" +
				"		\"fields\": \"text,title,description\"       \n" +
				"	}                                                \n" +
				"}                                                   \n"
				// @formatter:on
		)));
	}
}
