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

import static io.earcam.unexceptional.Exceptional.uri;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.Test;

public class SitemapParametersTest {

	@Test
	public void serialization() throws IOException
	{
		SitemapParameters parameters = new SitemapParameters(uri("https://acme.com/"), Paths.get("/", "tmp", "source"), Paths.get("/", "tmp", "target"));
		parameters.options().gzipped();
		parameters.options().setInclude(Pattern.compile(".*"));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		parameters.serialize(out);
		SitemapParameters rehydrated = new SitemapParameters();
		rehydrated.deserialize(new ByteArrayInputStream(out.toByteArray()));

		assertThat(parameters.base, is(equalTo(parameters.base)));
		assertThat(parameters.sourceDir, is(equalTo(parameters.sourceDir)));
		assertThat(parameters.targetDir, is(equalTo(parameters.targetDir)));
		assertThat(parameters.options().gzipped(), is(equalTo(parameters.options().gzipped())));
		assertThat(parameters.options().include(), is(equalTo(parameters.options().include())));
	}
}
