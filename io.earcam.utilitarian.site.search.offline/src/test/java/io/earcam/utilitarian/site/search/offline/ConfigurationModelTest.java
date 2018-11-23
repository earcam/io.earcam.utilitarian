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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.earcam.unexceptional.Exceptional;
import io.earcam.utilitarian.site.search.offline.ConfigurationModel.Crawling;
import io.earcam.utilitarian.site.search.offline.ConfigurationModel.Mapping;
import io.earcam.utilitarian.site.search.offline.jsonb.JsonBind;

public class ConfigurationModelTest {

	@Test
	public void mappingsToExpectedJson()
	{
		File dir = new File("./").getAbsoluteFile();
		URI uri = Exceptional.uri("https://acme.com/hum/bug/");

		Mapping mapping = new Mapping();
		mapping.setDir(dir);
		mapping.setUri(uri);

		List<Mapping> mappings = Collections.singletonList(mapping);

		Crawling crawling = new ConfigurationModel.Crawling();

		crawling.setMappings(mappings);

		String json = JsonBind.writeJson(crawling);

		assertThat(json, is(equalToIgnoringWhiteSpace(
				"{\n" +
						"    \"mappings\": [\n" +
						"        {\n" +
						"            \"dir\": \"" + dir + "\",\n" +
						"            \"uri\": \"https://acme.com/hum/bug/\"\n" +
						"        }\n" +
						"    ],\n" +
						"    \"steps\": null\n" +
						"}")));
	}
}
