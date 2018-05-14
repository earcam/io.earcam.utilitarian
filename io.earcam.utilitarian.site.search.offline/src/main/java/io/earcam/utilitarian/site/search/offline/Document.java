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

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.earcam.unexceptional.Exceptional;

//make this XmlRootElement - get Maven to pump config, copy 'n' paste for "main runner"
public class Document {
	public static final String LOCAL_FILE = "file";
	public static final String REF_URL = "url";

	public static final String RAW_TEXT = "raw";

	public static final String TEXT = "text";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String CONTENT_TYPE = "contentType";

	private final Map<String, String> fields = new HashMap<>();
	private final List<String> tokens = new ArrayList<>();
	private final Path file;
	private final String refUrl;


	private Document(Path file, String refUrl)
	{
		this.file = file;
		this.refUrl = refUrl;
	}


	public Path file()
	{
		return file;
	}


	public String raw()
	{
		return fields.getOrDefault(RAW_TEXT, "");
	}


	public boolean hasRaw()
	{
		return fields.containsKey(RAW_TEXT);
	}


	public List<String> tokens()
	{
		return tokens;
	}


	public boolean hasTokens()
	{
		return !tokens.isEmpty();
	}


	public String refUrl()
	{
		return refUrl;
	}


	@SuppressWarnings("squid:S1845")
	public String title()
	{
		return fields.getOrDefault(TITLE, "");
	}


	public void field(String key, String value)
	{
		fields.put(key, value);
	}


	public String field(String key)
	{
		return fields.getOrDefault(key, "");
	}


	public String contentType()
	{
		return fields.getOrDefault(CONTENT_TYPE, "");
	}


	public static Document document(Path baseDir, URI baseUri, Path file)
	{
		String relativeUri = ensureTrailingSlash(baseUri, true);
		String refUrl = relativizeReferenceUri(baseDir, relativeUri, file);

		Document document = new Document(file, refUrl);
		try {
			document.field(CONTENT_TYPE, Files.probeContentType(file));
		} catch(IOException e) {
			Exceptional.swallow(e);
		}
		return document;
	}


	public static String relativizeReferenceUri(Path baseDir, String baseUri, Path file)
	{
		return baseUri + baseDir.toUri().relativize(file.toUri()).toString();
	}


	private static String ensureTrailingSlash(URI uri, boolean uriPathOnly)
	{
		String yuri = uriPathOnly ? uri.getPath() : uri.toString();
		return (yuri.length() > 0 && yuri.charAt(yuri.length() - 1) == '/') ? yuri : (yuri + '/');
	}


	public Map<String, String> asMap()
	{
		HashMap<String, String> map = new HashMap<>(fields);
		map.put(REF_URL, refUrl);
		map.put(TEXT, tokens.stream().collect(joining(" ")));

		return map;
	}
}
