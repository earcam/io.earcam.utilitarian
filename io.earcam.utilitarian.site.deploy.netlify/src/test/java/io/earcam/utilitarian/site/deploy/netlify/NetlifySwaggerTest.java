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

import static io.earcam.utilitarian.site.deploy.netlify.SwaggerTestClient.createJaxRsTestClient;
import static io.earcam.utilitarian.site.deploy.netlify.SwaggerTestClient.swaggerApiTestClient;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;

import org.junit.Ignore;
import org.junit.Test;

import io.earcam.unexceptional.Exceptional;
import io.earcam.utilitarian.site.deploy.netlify.api.DefaultApi;
import io.earcam.utilitarian.site.deploy.netlify.api.domain.Deploy;
import io.earcam.utilitarian.site.deploy.netlify.api.domain.DeployFiles;
import io.earcam.utilitarian.site.deploy.netlify.api.domain.File;
import io.earcam.utilitarian.site.deploy.netlify.api.domain.Site;

@Ignore
public class NetlifySwaggerTest {
	private static final String ACCESS_TOKEN = "!YOUR_ACCESS_TOKEN_GOES_HERE!";

	static {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
		//		System.setProperty("org.slf4j.simpleLogger.log.org.eclipse.jetty", "info");
	}

	private static final Client JAX_RS_CLIENT = createJaxRsTestClient();

	private static final Path BASE_DIR = Paths.get(".", "src", "test", "resources", "wiremock", "api.netlify.com");

	//	@ClassRule
	//	public static WireMockClassRule WIRE_MOCK = new WireMockClassRule(options()
	//			.withRootDirectory(BASE_DIR.toAbsolutePath().toString())
	//			.port(findFreePort())
	//			.httpsPort(findFreePort()));
	//
	//	@Rule
	//	public WireMockClassRule wireMock = WIRE_MOCK;
	//
	//	private String baseUrl = "https://localhost:" + wireMock.httpsPort() + "/api/v1";

	//	private String baseUrl = "https://localhost:8443/api/v1";
	private String baseUrl = "https://api.netlify.com/api/v1/";


	//	@Ignore //Jackson wigs out as response json contains unrecognised field "site_id"
	@Test
	public void listSites() throws ApiException
	{
		DefaultApi client = apiClient();

		List<Site> sites = client.listSites();

		//System.out.println(sites);
	}


	private DefaultApi apiClient()
	{
		ApiClient apiClient = swaggerApiTestClient(baseUrl);
		apiClient.setAccessToken(ACCESS_TOKEN);
		DefaultApi api = new DefaultApi(apiClient);
		return api;
	}


	//	@Ignore
	@Test
	public void createSite() throws ApiException
	{
		DefaultApi client = apiClient();

		final String appName = "earcam-test";
		final String siteId = "test.earcam.io";

		Site site = new Site()
				.name(appName)
				.id(siteId) //TODO test this works
				.customDomain("test.earcam.io")
				//				.url("http://test.earcam.io")
				//				.ssl(true)
				//				.forceSsl(true)       // doesn't appear to work on site-creation
				.managedDns(false)    // true to use Netlify DNS
				.notificationEmail("earcam@gmail.com");

		Boolean configureDns = null;  //TODO NETLIFY API BUG - must be null or get a 500 everytime

		Site created = client.createSite(site, configureDns);

		//		System.out.println(created.getName() + " " + created.getCreatedAt());
		//		System.out.println(created);
		assertThat(created.getCreatedAt(), is(not(nullValue())));
	}


	@Test
	public void deleteSite() throws ApiException
	{
		//		final String siteId = "test.earcam.io";
		final String siteId = "613bcc50-2322-48dc-be89-c30d8f867508"; //TODO NETLIFY API BUG - can't use siteId as customDomain must be API ID

		DefaultApi client = apiClient();

		client.deleteSite(siteId);
	}


	@Ignore  //doesn't work
	@Test
	public void updateSiteSsl() throws ApiException
	{
		final String siteId = "test.earcam.io";

		DefaultApi client = apiClient();

		Site site = client.getSite(siteId)
				.managedDns(false)
				.ssl(true)
				.url("https://test.earcam.io");

		Site updated = client.updateSite(site.getId(), site);
		//		System.out.println("site: " + site);
		//		System.out.println("updated: " + updated);
		//		System.out.println("site.ssl: " + site.getSsl());
		//		System.out.println("updated.ssl: " + updated.getSsl());
	}


	@Test
	public void updateSiteSslViaHttpCall() throws ApiException
	{
		//		final String siteId = "test.earcam.io";
		final String siteId = "d0bd0c5d-2072-4f28-aeb8-b704158080c4";

		String url = baseUrl + "/sites/" + siteId + "/ssl";

		JAX_RS_CLIENT.target(url).request().header("Authorization", "Bearer " + ACCESS_TOKEN).post(Entity.json(null));

		DefaultApi client = apiClient();

		Site site = client.getSite(siteId);

		//		System.out.println("site: " + site);
	}


	@Ignore  //doesn't work
	@Test
	public void updateSiteForceSsl() throws ApiException
	{
		final String siteId = "test.earcam.io";

		DefaultApi client = apiClient();

		Site site = client.getSite(siteId);
		site.forceSsl(true);
		Site updated = client.updateSite(siteId, site);

		//		System.out.println("site.forceSsl: " + site.getForceSsl());
		//		System.out.println("updated.forceSsl: " + updated.getForceSsl());
	}


	@Test
	public void deployNewSite() throws ApiException, IOException
	{
		final String siteId = "test.earcam.io";
		//		final String siteId = "613bcc50-2322-48dc-be89-c30d8f867508"; //TODO NETLIFY API BUG - can't use siteId as customDomain must be API ID

		DefaultApi client = apiClient();

		Path baseDir = Paths.get("src", "test", "resources", "faux-site", "create");

		Map<String, List<java.io.File>> sha1FileMap = FileMap.sha1FileMap(baseDir);

		List<File> files = sha1FileMap.entrySet().stream()
				.flatMap(e -> e.getValue().stream()
						.map(java.io.File::toPath)
						.map(f -> {
							return new File()
									//						.id(e.getKey())
									.sha(e.getKey())
									.path("/" + baseDir.relativize(f).toString())
									.mimeType("text/html")
									.size(f.toFile().length());
						}))
				.collect(toList());

		DeployFiles deployRequest = new DeployFiles().files(files);//.async(false).draft(false);
		Deploy deployResponse = client.createSiteDeploy(siteId, deployRequest, "test-site-deploy-title" + UUID.randomUUID());

		List<String> required = deployResponse.getRequired();

		for(String sha : required) {
			List<java.io.File> uploads = sha1FileMap.get(sha);
			for(java.io.File upload : uploads) {
				Path file = upload.toPath();
				client.uploadDeployFile(deployResponse.getId(), "/" + baseDir.relativize(file).toString(), Exceptional.apply(Files::readAllBytes, file));
			}
		}
	}
}
