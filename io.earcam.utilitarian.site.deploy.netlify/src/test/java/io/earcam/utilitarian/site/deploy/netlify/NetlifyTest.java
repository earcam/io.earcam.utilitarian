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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.earcam.utilitarian.net.FreePortFinder.findFreePort;
import static io.earcam.utilitarian.site.deploy.netlify.Netlify.checkSuccessful;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.client.ClientBuilder.newBuilder;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.WillClose;
import javax.annotation.WillNotClose;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import io.earcam.unexceptional.Exceptional;
import io.earcam.utilitarian.net.ssl.DummySslContext;

public class NetlifyTest {

	private static final String ACCESS_TOKEN = "!YOUR_ACCESS_TOKEN_GOES_HERE!";

	static {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
		System.setProperty("org.slf4j.simpleLogger.log.org.eclipse.jetty", "warn");
	}
	private static final Logger LOG = LoggerFactory.getLogger(NetlifyTest.class);

	private static final Path MOCK_DIR = Paths.get(".", "src", "test", "resources", "wiremock", "api.netlify.com");

	@ClassRule
	public static WireMockClassRule WIRE_MOCK = new WireMockClassRule(options()
			.withRootDirectory(MOCK_DIR.toAbsolutePath().toString())
			.port(findFreePort())
			.httpsPort(findFreePort()));

	@Rule
	public WireMockClassRule wireMock = WIRE_MOCK;

	private String baseUrl = "https://localhost:" + wireMock.httpsPort() + "/api/v1/";  // test
	// private String baseUrl = "https://localhost:8443/api/v1/"; //wiremock proxy
	// private String baseUrl = "https://api.netlify.com/api/v1/"; //live

	private static final String SITE_ID = "34795e8e-8db5-4803-b711-190bb62f97c9";
	private static final String SITE_NAME = "earcam-test";

	private Client client = createJaxRsTestClient();

	private Netlify netlify = new Netlify(ACCESS_TOKEN, client, baseUrl);


	public static Client createJaxRsTestClient()
	{
		return newBuilder()
				.hostnameVerifier(DummySslContext.dummyHostnameVerifier())
				.sslContext(DummySslContext.unverifiedClientSslContext())
				.build();
	}


	@Test
	public void createSite()
	{
		Site site = new Site();
		site.setName("earcam-test");
		site.setCustomDomain("test.earcam.io");

		Site newSite = netlify.create(site);

		assertThat(newSite.id(), is(equalTo(SITE_ID)));
	}


	// TODO this should not return JsonArray but Site
	@Test
	public void listSites()
	{
		List<Site> sites = netlify.siteList();

		assertThat(sites, hasSize(5));
	}


	@Test
	public void findSiteId()
	{
		Site site = netlify.findSiteForName(SITE_NAME).orElseThrow(RuntimeException::new);;

		assertThat(site.id(), is(equalTo(SITE_ID)));
	}


	@Test
	public void destroySite()
	{
		netlify.destroy(SITE_NAME);
	}


	@Test
	public void attemptToDestroyUnknownSiteThrows()
	{
		try {
			netlify.destroy("UNKNOWN_SITE_NAME");
			fail();
		} catch(Exception e) {}
	}


	@Test
	public void deployZip() throws Exception
	{
		Path dir = Paths.get("src", "test", "resources", "faux-site", "create");

		netlify.deployZip(SITE_NAME, "", dir);
	}


	// TODO this should be in class and return Site
	@Test
	public void listDeploys()
	{
		Response response = client.target(baseUrl + "sites/" + SITE_ID + "/deploys")
				.request()
				.get();

		checkSuccessful(response);
	}

	private static final byte DOUBLE_QUOTE = '"';
	private static final byte[] COLON_SPACE = bytes(": ");
	private static final byte[] COMMA_NEWLINE = bytes(", " + lineSeparator());
	private Path baseDir;
	private Map<String, List<File>> sha1ToRelativePaths = new HashedMap<>();


	@Ignore // Can't work out what's wrong here - Swagger client suffers same issue, Java HTTP bug or Netlify?
	@Test
	public void deployNewSiteByDigest() throws Exception
	{
		Site site = netlify.findSiteForName("earcam-test").orElseThrow(RuntimeException::new);

		baseDir = Paths.get("src", "test", "resources", "faux-site", "create");

		sha1ToRelativePaths = FileMap.sha1FileMap(baseDir);

		StreamingOutput body = this::writeFileDigestsJson;

		Response response = client.target(baseUrl + "sites/" + site.id() + "/deploys")
				.request(APPLICATION_JSON_TYPE)
				.post(Entity.entity(body, APPLICATION_JSON_TYPE));

		checkSuccessful(response);

		JsonObject json = response.readEntity(JsonObject.class);
		LOG.debug("{}", json);
		String deployId = json.getString("id");
		JsonArray required = json.getJsonArray("required");

		LOG.debug("deployId: {}", deployId);
		LOG.debug("required: {}", required);

		// String deployId = "59820b490752d01d702d1bd3";
		// List<String> required = Arrays.asList("195B3BB329664FF5C3282D7C229926349008D56B",
		// "C08D599B19D4CA5F31CD0F65C2642549E82BA42D");

		required.stream()
				.map(JsonString.class::cast)
				.map(JsonString::getString)
				/// required.stream()
				.peek(LOG::trace)
				.map(sha1ToRelativePaths::get)
				.peek(f -> LOG.trace("got from SHA map {}", f))
				.flatMap(List::stream)
				.peek(l -> LOG.trace("WTF??? {}", l))
				.forEach(f -> deployFile(deployId, f));

	}


	private void deployFile(String deployId, File file)
	{
		LOG.trace("Deploying {} for deployment ID: {}", file, deployId);
		StreamingOutput body = o -> Exceptional.accept(NetlifyTest::inputToOutput, new FileInputStream(file), (OutputStream) o);

		Response response = client.target(baseUrl + "deploys/" + deployId + "/files/" + relativize(file))
				.request(APPLICATION_JSON_TYPE)
				.put(Entity.entity(body, APPLICATION_OCTET_STREAM_TYPE));

		LOG.trace("deploy file response: {}", response.readEntity(String.class));

		checkSuccessful(response);
	}


	private static void inputToOutput(@WillClose InputStream in, @WillNotClose OutputStream out) throws IOException
	{
		int b;
		long c = 0;
		try(InputStream input = in) {
			while((b = in.read()) != -1) {
				out.write(b);
				c++;
			}
		}
		LOG.debug("Wrote {} bytes", c);
	}


	/*
	 * {"files": {"/index.html": "907d14fb3af2b0d4f18c2d46abe8aedce17367bd"}}
	 */
	private void writeFileDigestsJson(OutputStream output) throws IOException
	{
		output.write(bytes("{\"files\": { "));
		Iterator<Entry<String, List<File>>> it = sha1ToRelativePaths.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, List<File>> entry = it.next();
			for(File file : entry.getValue()) {
				output.write(DOUBLE_QUOTE);
				output.write(bytes(relativize(file)));
				output.write(DOUBLE_QUOTE);
				output.write(COLON_SPACE);
				output.write(DOUBLE_QUOTE);
				output.write(bytes(entry.getKey()));
				output.write(DOUBLE_QUOTE);
				if(it.hasNext()) {
					output.write(COMMA_NEWLINE);
				}
			}
		}
		output.write(bytes("} }"));
	}


	private static byte[] bytes(String text)
	{
		return text.getBytes(UTF_8);
	}


	private String relativize(File path)
	{
		return baseDir.toUri().relativize(path.toPath().toUri()).toString();
	}
}
