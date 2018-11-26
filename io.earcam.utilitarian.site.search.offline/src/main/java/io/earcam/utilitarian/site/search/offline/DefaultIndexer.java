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

import static io.earcam.unexceptional.Closing.closeAfterAccepting;
import static io.earcam.unexceptional.Exceptional.apply;
import static io.earcam.utilitarian.site.search.offline.Component.getOrDefault;
import static io.earcam.utilitarian.site.search.offline.Component.mandatory;
import static io.earcam.utilitarian.site.search.offline.Javascript.createJavascriptEngine;
import static io.earcam.utilitarian.site.search.offline.Javascript.invokeFunction;
import static io.earcam.utilitarian.site.search.offline.Resources.SCRIPT_INDEX;
import static io.earcam.utilitarian.site.search.offline.Resources.SCRIPT_SEARCH;
import static io.earcam.utilitarian.site.search.offline.Resources.getResource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.WillNotClose;
import javax.script.Invocable;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO test maven SOURCE filtering to replace VERSION_* constants ... but will IDE/Eclipse do the replacement before test exec?
public class DefaultIndexer implements Indexer {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultIndexer.class);

	public static final String BASEDIR_WEBJARS_RESOURCES = "META-INF/resources/webjars/";
	public static final String VERSION_LUNR_JS = "2.1.0";
	public static final String LUNR = BASEDIR_WEBJARS_RESOURCES + "lunr.js/" + VERSION_LUNR_JS + "/lunr.js";

	public static final String OUTPUT_FILE = "outputFile";
	public static final String FIELDS = "fields";
	public static final String MAP_TITLES = "mapTitles";
	public static final String GENERATE_AUTOCOMPLETE = "generateAutocomplete";
	public static final String OUTPUT_CHARSET = "outputCharset";

	private Path outputFile;

	@SuppressWarnings("squid:S1845")
	private String[] fields;
	private String refUrl;
	private Charset outputCharset;

	private Map<String, String> titlesMap = new HashMap<>();
	private SortedSet<String> autocomplete = new TreeSet<>();

	private BiConsumer<String, String> titleMapper = titlesMap::put;
	private Consumer<String> autocompleter = autocomplete::add;

	private Invocable engine;
	private Object javascriptIndexBuilder;


	@Override
	public void configure(Map<String, String> configuration)
	{
		outputCharset = getOrDefault(configuration, OUTPUT_CHARSET, UTF_8);
		refUrl = mandatory(configuration, Document.REF_URL);
		outputFile = Paths.get(mandatory(configuration, OUTPUT_FILE));
		fields = mandatory(configuration, FIELDS).split(",");

		if(!getOrDefault(configuration, MAP_TITLES, true)) {
			titleMapper = (u, t) -> { /* noop */ };
		}

		if(!getOrDefault(configuration, GENERATE_AUTOCOMPLETE, true)) {
			autocompleter = d -> { /* noop */ };
		}

		initialize();
	}


	private void initialize()
	{
		engine = createSearchEngine(SCRIPT_INDEX);

		Map<String, Map<Object, Object>> fieldConfigurations = stream(fields).collect(toMap(identity(), v -> emptyMap()));
		javascriptIndexBuilder = invokeFunction(engine, "createIndexBuilder", refUrl, fieldConfigurations);
	}


	static Invocable createSearchEngine(String script)
	{
		InputStream lunr = getResource(LUNR);
		InputStream indexScript = getResource(script);
		Objects.requireNonNull(lunr, "Could not load lunrjs lib");
		Objects.requireNonNull(indexScript, "Could not load indexScript");
		return createJavascriptEngine(lunr, indexScript);
	}


	@Override
	public synchronized Indexer add(Stream<Document> documents)
	{
		List<Document> filtered = documents.filter(Document::hasTokens).collect(toList());
		filtered.forEach(this::addToTitleAndAutoComplete);

		invokeFunction(engine, "addDocuments", javascriptIndexBuilder, filtered.stream()
				.map(Document::asMap)
				.iterator());
		return this;
	}


	private void addToTitleAndAutoComplete(Document document)
	{
		titleMapper.accept(document.refUrl(), document.title());
		document.tokens().forEach(autocompleter::accept);
	}


	@Override
	public void writeJson()
	{
		outputFile.getParent().toFile().mkdirs();

		if(isGzip()) {
			FileOutputStream fos = apply(FileOutputStream::new, outputFile.toFile());
			closeAfterAccepting(GZIPOutputStream::new, fos, this::writeJson);
		} else {
			closeAfterAccepting(FileOutputStream::new, outputFile.toFile(), this::writeJson);
		}
	}


	private boolean isGzip()
	{
		return outputFile.getFileName().toString().endsWith(".gz");
	}


	protected void writeJson(@WillNotClose OutputStream output) throws IOException
	{
		writeIndex(output);
		writeAutocomplete(output);
		writeTitleMap(output);
	}


	private void writeIndex(OutputStream output) throws IOException
	{
		output.write(bytes("{\n\n\"index\": "));
		String indexJson = serializeIndex();
		byte[] bytes = bytes(indexJson);
		output.write(bytes);
		String id = id();
		LOG.debug("{} wrote {} bytes for index to {}", id, bytes.length, outputFile);
	}


	public byte[] bytes(String text)
	{
		return text.getBytes(outputCharset);
	}


	public String serializeIndex()
	{
		return (String) invokeFunction(engine, "buildSerializedIndex", javascriptIndexBuilder);
	}


	private void writeAutocomplete(OutputStream output) throws IOException
	{
		byte[] bytes = bytes(autocomplete.stream().collect(joining("\", \"", ",\n\n\"autocomplete\": [\"", "\"]")));
		output.write(bytes);
		String id = id();
		LOG.debug("{} wrote {} bytes for {} words for autocomplete to {}", id, bytes.length, autocomplete.size(), outputFile);
	}


	private void writeTitleMap(OutputStream output) throws IOException
	{
		byte[] bytes = bytes(titlesMap.entrySet().stream().map(
				e -> new StringBuilder()
						.append('"').append(e.getKey()).append('"')
						.append(':')
						.append('"').append(e.getValue()).append('"'))
				.collect(joining(", ", ",\n\n\"titleMap\": {", "}\n}")));
		output.write(bytes);
		String id = id();
		LOG.debug("{} wrote {} bytes for {} entries for title map to {}", id, bytes.length, titlesMap.size(), outputFile);
	}


	public static String search(String indexJson, String query) throws ScriptException, NoSuchMethodException
	{
		Invocable engine = createSearchEngine(SCRIPT_SEARCH);
		return engine.invokeFunction("jsonSearchIndex", indexJson, query).toString();
	}
}
