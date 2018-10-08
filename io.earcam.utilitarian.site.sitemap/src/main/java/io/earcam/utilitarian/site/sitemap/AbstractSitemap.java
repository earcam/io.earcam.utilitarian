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

import static io.earcam.unexceptional.Exceptional.uncheckConsumer;
import static io.earcam.utilitarian.io.SplittableOutputStream.splittable;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import io.earcam.unexceptional.Exceptional;
import io.earcam.utilitarian.io.SplittableOutputStream;

public abstract class AbstractSitemap {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + lineSeparator();

	private static final byte[] TAG_LASTMOD = bytes("lastmod");

	private static final byte[] TAG_LOC = bytes("loc");

	private static final byte[] NL = bytes(lineSeparator());

	private static final byte TAB = '\t';

	protected final SitemapParameters parameters;

	long maxSize = 50L * 1000L * 1000L;
	long maxCount = 50L * 1000L * 1000L;

	int indexFileSuffix = 0; // we don't know the number of output files apriori, so rename operation at end

	private byte[] head;

	private byte[] tail;

	private byte[] recordTag;

	private SplittableOutputStream output;

	private Consumer<Path> generatedFileRecorder;


	protected AbstractSitemap(SitemapParameters parameters, String head, String recordTag, String tail, Consumer<Path> generatedFileRecorder)
	{
		this.parameters = parameters;
		this.head = bytes(XML_HEADER + head + lineSeparator());
		this.recordTag = bytes(recordTag);
		this.tail = bytes(tail + lineSeparator());
		this.generatedFileRecorder = generatedFileRecorder;
	}


	static byte[] bytes(String text)
	{
		return text.getBytes(UTF_8);
	}


	protected abstract Path filename();


	protected abstract String createUrl(Path sitemap) throws IOException;


	protected void process(Stream<Path> sitemaps) throws IOException
	{
		try {
			createSplittableOutputStream();
			sitemaps.sequential().forEach(uncheckConsumer(this::writeSitemapEntry));
		} finally {
			closeSplittableOutputStream();
		}
	}


	private void createSplittableOutputStream() throws IOException
	{
		output = splittable(this::createOutputStream, head, tail)
				.maxSize(maxSize)
				.maxCount(maxCount)
				.outputStream();
	}


	private void closeSplittableOutputStream() throws IOException
	{
		output.close();
	}


	private OutputStream createOutputStream()
	{
		Path indexFile = filename();
		generatedFileRecorder.accept(indexFile);
		return createOutputStream(indexFile);
	}


	protected Path filename(SitemapParameters parameters, String name)
	{
		return parameters.targetDir.resolve(name + ".xml" + (parameters.options().gzipped() ? ".gz" : ""));
	}


	private OutputStream createOutputStream(Path indexFile)
	{
		OutputStream stream = Exceptional.apply(FileOutputStream::new, indexFile.toFile());
		if(parameters.options().gzipped()) {
			stream = Exceptional.apply(GZIPOutputStream::new, stream);
		}
		return stream;
	}


	private void writeSitemapEntry(Path path) throws IOException
	{
		output.beginRecord();

		writeIndent();
		writeOpenTag(recordTag);
		output.write(NL);

		writeDoubleIndent();
		writeOpenTag(TAG_LOC);
		writeText(createUrl(path));
		writeCloseTag(TAG_LOC);

		writeDoubleIndent();
		writeOpenTag(TAG_LASTMOD);
		writeText(lastModified(path));
		writeCloseTag(TAG_LASTMOD);

		writeIndent();
		writeCloseTag(recordTag);

		output.endRecord();
	}


	private void writeIndent() throws IOException
	{
		output.write(TAB);
	}


	private void writeOpenTag(byte[] tag) throws IOException
	{
		output.write('<');
		output.write(tag);
		output.write('>');
	}


	private void writeDoubleIndent() throws IOException
	{
		writeIndent();
		writeIndent();
	}


	private void writeText(String text) throws IOException
	{
		output.write(bytes(text));
	}


	private void writeCloseTag(byte[] tag) throws IOException
	{
		output.write('<');
		output.write('/');
		output.write(tag);
		output.write('>');
		output.write(NL);
	}


	static String lastModified(Path file)
	{
		return lastModifiedDateTime(file).toLocalDate().toString();
	}


	static LocalDateTime lastModifiedDateTime(Path file)
	{
		return LocalDateTime.ofInstant(Exceptional.apply(Files::getLastModifiedTime, file).toInstant(), ZoneId.systemDefault());
	}
}
