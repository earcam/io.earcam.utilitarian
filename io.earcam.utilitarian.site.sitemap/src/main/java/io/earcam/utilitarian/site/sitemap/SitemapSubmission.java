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

import static io.earcam.unexceptional.Exceptional.uncheckFunction;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class SitemapSubmission {

	private SitemapSubmission()
	{}


	/**
	 *
	 * @param hosts A list of search engine hosts (including protocol, e.g. https://google.com)
	 * @param base Website
	 * @param targetDir
	 * @param sitemaps
	 * @return
	 */
	public static String submit(List<String> hosts, URI base, Path targetDir, Stream<Path> sitemaps)
	{
		List<String> hostsApis = hostApis(hosts);

		return sitemaps.map(uncheckFunction(s -> submit(hostsApis, base, targetDir, s)))
				.collect(joining());
	}


	private static List<String> hostApis(List<String> hosts)
	{
		return hosts.stream().map(h -> h + "/ping?sitemap=").collect(toList());
	}


	private static String submit(List<String> hostsApis, URI base, Path targetDir, Path sitemap) throws IOException, URISyntaxException
	{
		StringBuilder response = new StringBuilder();
		URI relative = targetDir.toUri().relativize(sitemap.toUri());
		URI submission = base.resolve(relative);

		for(String hostApi : hostsApis) {
			URI uri = new URI(hostApi + URLEncoder.encode(submission.toString(), UTF_8.toString()));

			HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

			connection.getInputStream().close();

			response.append(uri)
					.append(" - ")
					.append(connection.getResponseCode())
					.append(": ")
					.append(connection.getResponseMessage())
					.append(",  ")
					.append(lineSeparator());
		}
		return response.toString();
	}
}
