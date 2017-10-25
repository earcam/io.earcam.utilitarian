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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import io.earcam.unexceptional.Closing;
import io.earcam.utilitarian.io.ReplaceAllOutputStream;

public final class Resources {

	public static final String UI_SCRIPT_SEARCH_FILE = "ui.search.lunr.js";

	public static final String RESOURCES_ROOT = "META-INF/resources/js/";

	public static final String SCRIPT_INDEX = RESOURCES_ROOT + "index.lunr.js";

	public static final String UI_SCRIPT_SEARCH = RESOURCES_ROOT + UI_SCRIPT_SEARCH_FILE;

	static final String SCRIPT_SEARCH = RESOURCES_ROOT + "json.search.lunr.js";

	public static final String CONFIGURATION_ROOT = "META-INF/configuration/";

	public static final String DEFAULT_CRAWLER_JSON = CONFIGURATION_ROOT + "default-crawler-maven.json";
	public static final String DEFAULT_INDEXER_JSON = CONFIGURATION_ROOT + "default-indexer-maven.json";


	private Resources()
	{}


	public static InputStream getResource(String resource)
	{
		return Resources.class.getClassLoader().getResourceAsStream(resource);
	}


	public static String getResource(String resource, Charset charset, Map<String, String> searchReplace)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream output = baos;

		for(Map.Entry<String, String> e : searchReplace.entrySet()) {
			output = new ReplaceAllOutputStream(e.getKey().getBytes(charset), e.getValue().getBytes(charset), output);
		}
		Closing.closeAfterAccepting(getResource(resource), output, Resources::drain);
		return new String(baos.toByteArray(), charset);
	}


	private static void drain(InputStream input, OutputStream output) throws IOException
	{
		int b;
		while((b = input.read()) != -1) {
			output.write(b);
		}
	}
}
