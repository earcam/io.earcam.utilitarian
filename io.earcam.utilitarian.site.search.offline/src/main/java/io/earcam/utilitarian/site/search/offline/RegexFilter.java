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

import static io.earcam.utilitarian.site.search.offline.Component.mandatory;
import static java.util.regex.Pattern.compile;

import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(RegexFilter.class);

	public static final String INCLUDE = "include";
	public static final String EXCLUDE = "exclude";

	public final String id = id();
	private Predicate<String> predicate;


	@Override
	public void configure(Map<String, String> configuration)
	{
		String include = mandatory(configuration, INCLUDE);
		String exclude = mandatory(configuration, EXCLUDE);

		predicate = compile(include).asPredicate().and(compile(exclude).asPredicate().negate());
		LOG.debug("{} include pattern: {}", id, include);
		LOG.debug("{} exclude pattern: {}", id, exclude);
	}


	@Override
	public boolean test(Document document)
	{
		String path = document.file().toAbsolutePath().toString();
		boolean include = predicate.test(path);
		LOG.debug("{} {}cluding {}", id, include ? "in" : "ex", path);
		return include;
	}
}
