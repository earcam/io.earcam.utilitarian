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

import static io.earcam.utilitarian.site.search.offline.RegexFilter.EXCLUDE;
import static io.earcam.utilitarian.site.search.offline.RegexFilter.INCLUDE;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.earcam.unexceptional.Exceptional;

public class RegexFilterTest {

	private final RegexFilter filter = new RegexFilter();

	private final String exclude = ".*\\.(unwanted|bad|tmp|.*~)$";
	private final String include = ".*";


	@Test
	public void includeIsMandatory() throws Exception
	{
		try {
			filter.configure(singletonMap(EXCLUDE, exclude));
			fail();
		} catch(NullPointerException e) {}
	}


	@Test
	public void excludeIsMandatory() throws Exception
	{
		try {
			filter.configure(singletonMap(INCLUDE, include));
			fail();
		} catch(NullPointerException e) {}
	}


	@Test
	public void includes() throws Exception
	{
		filter.configure(map());
		Document document = Document.document(Paths.get("."), Exceptional.uri("/"), Paths.get("this", "is", "o.k"));

		assertThat(filter.test(document), is(true));
	}


	private Map<String, String> map()
	{
		Map<String, String> configuration = new HashMap<>();
		configuration.put(EXCLUDE, exclude);
		configuration.put(INCLUDE, include);
		return configuration;
	}


	@Test
	public void excludes() throws Exception
	{
		filter.configure(map());
		Document document = Document.document(Paths.get("."), Exceptional.uri("/"), Paths.get("this", "is", "not.for.inclusion~"));

		assertThat(filter.test(document), is(false));
	}
}
