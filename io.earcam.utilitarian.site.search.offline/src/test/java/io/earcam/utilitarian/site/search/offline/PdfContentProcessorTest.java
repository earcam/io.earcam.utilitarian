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

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import io.earcam.unexceptional.Exceptional;

public class PdfContentProcessorTest {

	@Test
	public void processesContent() throws Exception
	{
		Path baseDir = Paths.get(".", "src", "test", "resources", "dummysite");
		Path file = baseDir.resolve("pdf-no-meta.pdf");

		Document document = Document.document(baseDir, Exceptional.uri("/"), file);

		PdfContentProcessor parser = new PdfContentProcessor();

		parser.process(document);

		assertThat(document.raw(), is(equalToIgnoringWhiteSpace("test")));
	}


	// @Ignore // pdfbox fail for meta - on OSX only??
	@Test
	public void processesContentAndMeta() throws Exception
	{
		Path baseDir = Paths.get(".", "src", "test", "resources", "dummysite");
		Path file = baseDir.resolve("pdf-with-meta.pdf");

		Document document = Document.document(baseDir, Exceptional.uri("/"), file);

		PdfContentProcessor parser = new PdfContentProcessor();

		parser.process(document);

		assertThat(document.raw(), is(equalToIgnoringWhiteSpace("This is not the title blah blah")));
		assertThat(document.field(Document.TITLE), is(equalToIgnoringWhiteSpace("This is the title")));
	}


	@Test
	public void willNotAttemptToProcessNonPdfContent() throws Exception
	{
		Path baseDir = Paths.get(".", "src", "test", "resources", "dummysite");
		Path file = baseDir.resolve("index.html");

		Document document = Document.document(baseDir, Exceptional.uri("/"), file);

		PdfContentProcessor parser = new PdfContentProcessor();

		parser.process(document);

		assertThat(document.raw(), is(emptyString()));
	}

}
