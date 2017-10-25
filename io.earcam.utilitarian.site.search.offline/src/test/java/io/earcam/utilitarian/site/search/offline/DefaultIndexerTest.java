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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.io.FileMatchers.aFileWithSize;
import static org.hamcrest.io.FileMatchers.aReadableFile;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

import io.earcam.unexceptional.Exceptional;

public class DefaultIndexerTest {

	private final DefaultIndexer indexer = new DefaultIndexer();


	@Test
	public void autoCompleteEnabledByDefault() throws IOException
	{
		indexer.configure(defaultConfiguration());
		indexer.add(defaultDocuments());

		String json = writeJsonToString();

		assertThat(json, containsString(defaultExpectedAutocomplete()));
	}


	private Map<String, String> defaultConfiguration()
	{
		Map<String, String> configuration = new HashMap<>();
		configuration.put(Document.REF_URL, Document.REF_URL);
		String fields = Document.TEXT + ',' + Document.TITLE + ',' + Document.DESCRIPTION;
		configuration.put(DefaultIndexer.FIELDS, fields);
		configuration.put(DefaultIndexer.OUTPUT_FILE, "/dev/null");
		return configuration;
	}


	private Stream<Document> defaultDocuments()
	{
		URI uri = Exceptional.uri("https://meh.acme.com/context-root/");
		Path baseDir = Paths.get("some", "path");

		Document document1 = Document.document(baseDir, uri,
				Paths.get("some", "path", "to", "a.file"));
		Document document2 = Document.document(baseDir, uri,
				Paths.get("some", "path", "to", "be_or_not", "2b.a.file"));

		document1.tokens().addAll(Arrays.asList("some", "content", "goes", "here"));
		document2.tokens().addAll(Arrays.asList("some", "more", "content", "goes", "here"));

		document1.field(Document.TITLE, "document1");
		document2.field(Document.TITLE, "document2");

		Stream<Document> documents = Stream.of(document1, document2);
		return documents;
	}


	private String writeJsonToString() throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		indexer.writeJson(output);
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}


	private String defaultExpectedAutocomplete()
	{
		return Stream.of("some", "more", "content", "goes", "here")
				.sorted()
				.collect(joining("\", \"", "[\"", "\"]"));
	}


	@Test
	public void titleMapEnabledByDefault() throws IOException
	{
		indexer.configure(defaultConfiguration());
		indexer.add(defaultDocuments());

		String json = writeJsonToString();

		assertThat(json, allOf(
				containsString("\"/context-root/to/be_or_not/2b.a.file\":\"document2\""),
				containsString("\"/context-root/to/a.file\":\"document1\"}")));
	}


	@Test
	public void autoCompleteMayBeDisabled() throws IOException
	{
		Map<String, String> configuration = defaultConfiguration();
		configuration.put(DefaultIndexer.GENERATE_AUTOCOMPLETE, Boolean.FALSE.toString());
		indexer.configure(configuration);
		indexer.add(defaultDocuments());

		String json = writeJsonToString();

		assertThat(json, containsString("\"autocomplete\": [\"\"]"));
	}


	@Test
	public void titleMapMayBeDisabled() throws IOException
	{
		Map<String, String> configuration = defaultConfiguration();
		configuration.put(DefaultIndexer.MAP_TITLES, Boolean.FALSE.toString());
		indexer.configure(configuration);
		indexer.add(defaultDocuments());

		String json = writeJsonToString();

		assertThat(json, containsString("\"titleMap\": {}"));
	}


	@Test
	public void writesJsonToFileSystem() throws IOException
	{
		Map<String, String> configuration = defaultConfiguration();
		Path output = Paths.get(".", "target", getClass().getCanonicalName(), UUID.randomUUID().toString(), "output.json");
		assertThat(output.toFile(), is(not(aReadableFile())));

		configuration.put(DefaultIndexer.OUTPUT_FILE, output.toAbsolutePath().toString());
		indexer.configure(configuration);
		indexer.add(defaultDocuments());

		indexer.writeJson();

		assertThat(output.toFile(), is(aFileWithSize(greaterThan(100L))));
	}


	@Test
	public void failsToWriteJsonIfOutputFileIsADirectory() throws IOException
	{
		Map<String, String> configuration = defaultConfiguration();
		Path output = Paths.get(".", "target", getClass().getCanonicalName(), UUID.randomUUID().toString(), "output.dir");
		output.toFile().mkdirs();

		configuration.put(DefaultIndexer.OUTPUT_FILE, output.toAbsolutePath().toString());
		indexer.configure(configuration);
		indexer.add(defaultDocuments());

		try {
			indexer.writeJson();
			fail();
		} catch(UncheckedIOException e) {}

	}


	@Test
	public void writesGzippedJsonToFileSystem() throws IOException
	{
		Map<String, String> configuration = defaultConfiguration();
		Path output = Paths.get(".", "target", getClass().getCanonicalName(), UUID.randomUUID().toString(), "output.json.gz");
		assertThat(output.toFile(), is(not(aReadableFile())));

		configuration.put(DefaultIndexer.OUTPUT_FILE, output.toAbsolutePath().toString());
		indexer.configure(configuration);
		indexer.add(defaultDocuments());

		indexer.writeJson();

		assertThat(output.toFile(), is(aFileWithSize(greaterThan(100L))));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(GZIPInputStream input = new GZIPInputStream(new FileInputStream(output.toFile()))) {
			int b;
			while((b = input.read()) != -1) {
				baos.write(b);
			}
		}
		String json = new String(baos.toByteArray(), UTF_8);

		assertThat(json, containsString(defaultExpectedAutocomplete()));
	}
}
