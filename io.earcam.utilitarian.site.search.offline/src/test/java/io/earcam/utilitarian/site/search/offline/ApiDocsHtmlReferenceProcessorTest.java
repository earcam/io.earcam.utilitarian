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

import static io.earcam.unexceptional.Exceptional.uri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class ApiDocsHtmlReferenceProcessorTest {

	private final ApiDocsHtmlReferenceProcessor processor = new ApiDocsHtmlReferenceProcessor();

	private final Path baseDir = Paths.get("base", "dir");


	@Test
	public void skipped() throws Exception
	{
		Path file = baseDir.resolve(Paths.get("apidocs", "com", "acme", "foo", "package-summary.html"));
		Document document = createDocument(file);

		processor.process(document);

		assertThat(document.hasRaw(), is(true));
		assertThat(document.raw(), is(emptyString()));
		assertThat(document.tokens(), is(empty()));
	}


	private Document createDocument(Path file)
	{
		Document document = Document.document(baseDir, uri("/base/uri/"), file);
		document.field(Document.CONTENT_TYPE, AbstractHtmlProcessor.TEXT_HTML);
		return document;
	}


	@Test
	public void processed() throws Exception
	{
		Path file = baseDir.resolve(Paths.get("apidocs", "com", "acme", "foo", "SomeClass.html"));
		Document document = createDocument(file);

		processor.process(document);

		assertThat(document.hasRaw(), is(true));

		assertThat(document.title(), containsString("SomeClass"));
		assertThat(document.tokens(), containsInAnyOrder("com.acme.foo.SomeClass", "SomeClass"));
	}
}
