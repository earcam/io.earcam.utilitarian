/*-
 * #%L
 * io.earcam.utilitarian.site.deploy.netlify
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
package io.earcam.utilitarian.site.deploy.netlify;

import static io.earcam.unexceptional.Closing.closeAfterAccepting;
import static java.time.Instant.MIN;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.client.ClientBuilder.newBuilder;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.WillClose;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.earcam.unexceptional.CheckedConsumer;
import io.earcam.unexceptional.EmeticStream;
import io.earcam.utilitarian.web.jaxrs.JsonMessageBodyReader;
import io.earcam.utilitarian.web.jaxrs.JsonMessageBodyWriter;
import io.earcam.utilitarian.web.jaxrs.TokenBearerAuthenticator;
import io.earcam.utilitarian.web.jaxrs.UserAgent;

/**
 * API client for <a href="https://netlify.com">Netlify</a>
 */
public class Netlify {

	private static final String USER_AGENT = "Mozilla/5.0 (X11; YouNix; Linux x86_64; rv:53.0) earcam.io/1.0";

	private static final MediaType APPLICATION_ZIP_TYPE = new MediaType("application", "zip");

	public static final String BASE_URL = "https://api.netlify.com/api/v1/";

	private static final Logger LOG = LoggerFactory.getLogger(Netlify.class);

	private Client client;
	private String baseUrl;


	public Netlify(String accessToken)
	{
		this(accessToken, newBuilder().build(), BASE_URL);
	}


	public Netlify(String accessToken, String baseUrl)
	{
		this(accessToken, newBuilder().build(), baseUrl);
	}


	public Netlify(String accessToken, Client client, String baseUrl)
	{
		this.client = configure(accessToken, client);
		this.baseUrl = ensureTrailingSlash(baseUrl);
	}


	private static Client configure(String accessToken, Client client)
	{
		return client.register(new TokenBearerAuthenticator(accessToken))
				.register(new UserAgent(USER_AGENT))
				.register(new JsonMessageBodyReader())
				.register(new JsonMessageBodyWriter());
	}


	private String ensureTrailingSlash(String earl)
	{
		return earl.charAt(earl.length() - 1) == '/' ? earl : earl + '/';
	}


	public Site create(Site site)
	{
		StreamingOutput o = site::writeJson;

		Response response = client.target(baseUrl + "sites")
				.request(APPLICATION_JSON_TYPE)
				.post(Entity.entity(o, APPLICATION_JSON_TYPE));

		checkSuccessful(response);

		JsonObject json = response.readEntity(JsonObject.class);
		return Site.fromJsonObject(json);
	}


	static void checkSuccessful(Response response)
	{
		if(response.getStatusInfo().getFamily() != SUCCESSFUL) {  // should be a ???
			throw requestFailedException(response);
		}
	}


	static IllegalStateException requestFailedException(Response response)
	{
		return new IllegalStateException(response.getStatus() + " - " +
				response.getStatusInfo().getReasonPhrase());
	}


	public List<Site> list()
	{
		throw new UnsupportedOperationException("TODO");
	}


	public void destroy(String siteName)
	{
		Site site = siteForName(siteName);

		Response response = client.target(baseUrl + "sites/" + site.id())
				.request()
				.delete();

		checkSuccessful(response);
	}


	private Site siteForName(String siteName)
	{
		return findSiteForName(siteName).orElseThrow(() -> new RuntimeException("No site found with name: " + siteName));
	}


	public void deployZip(String siteName, String uploadPath, Path baseDir)
	{
		deployZip(siteName, singletonMap(uploadPath, baseDir));
	}


	public void deployZip(String siteName, Map<String, Path> baseDirs)
	{
		Site site = siteForName(siteName);
		deploySiteZip(baseDirs, site);
	}


	protected void deploySiteZip(Map<String, Path> baseDirs, Site site)
	{
		StreamingOutput body = o -> writeZip(baseDirs, o);

		Response response = client.target(baseUrl + "sites/" + site.id() + "/deploys")
				.request(APPLICATION_JSON_TYPE)
				.post(Entity.entity(body, APPLICATION_ZIP_TYPE));

		LOG.debug("response:  {}", response);
		checkSuccessful(response);

		JsonObject json = response.readEntity(JsonObject.class);

		String state = json.getString("state", "unknown");
		if(!"uploaded".equals(state)) {
			throw new IllegalStateException("Response JSON doesn't include 'state=\"uploaded\"', " + state + ".  JSON: " + json);
		}
	}


	protected void writeZip(Map<String, Path> baseDirs, @WillClose OutputStream output)
	{
		closeAfterAccepting(ZipOutputStream::new, output, baseDirs, this::doWriteZip);
	}


	private void doWriteZip(ZipOutputStream zip, Map<String, Path> baseDirs)
	{
		@SuppressWarnings("squid:S1905") // false positive; cast IS required
		CheckedConsumer<byte[], IOException> writeThenClose = ((CheckedConsumer<byte[], IOException>) zip::write).andThen(b -> zip.closeEntry());

		for(Entry<String, Path> e : baseDirs.entrySet()) {
			EmeticStream.emesis(Files::walk, e.getValue())
					.sequential()
					.sorted(Path::compareTo)
					.filter(Files::isRegularFile)
					// Quick hack, TODO add excludes, but sitemap should also clean up it's cache files
					.filter(p -> !p.getFileName().toString().startsWith(".io.earcam.utilitarian.site.sitemap."))
					.peek(f -> LOG.debug("Writing to zip: {}", f))
					.peek(f -> zipEntry(zip, e, f))
					.map(Files::readAllBytes)
					.forEach(writeThenClose);
		}
	}


	/*
	 * Timestamps set to constant values purely to make wire-mock testing easy (irrelevant for Netlify)
	 */
	private void zipEntry(ZipOutputStream zip, Entry<String, Path> baseDir, Path file) throws IOException
	{
		URI relativePath = baseDir.getValue().toUri().relativize(file.toUri());
		String absolutePath = baseDir.getKey() + relativePath;
		ZipEntry entry = new ZipEntry(absolutePath);
		LOG.debug("Absolute path in site: /{}", absolutePath);
		entry.setTime(0);
		entry.setCreationTime(FileTime.from(MIN));
		entry.setLastAccessTime(FileTime.from(MIN));
		entry.setLastModifiedTime(FileTime.from(MIN));
		zip.putNextEntry(entry);
	}


	public Optional<Site> findSiteForName(String siteName)
	{
		List<Site> json = siteList();

		return json.stream()
				.filter(s -> siteName.equals(s.name()))
				.findAny();
	}


	List<Site> siteList()
	{
		return client.target(baseUrl + "sites")
				.request(APPLICATION_JSON_TYPE)
				.get()
				.readEntity(JsonArray.class)
				.stream()
				.map(JsonValue::asJsonObject)
				.map(Site::fromJsonObject)
				.collect(Collectors.toList());
	}
}
