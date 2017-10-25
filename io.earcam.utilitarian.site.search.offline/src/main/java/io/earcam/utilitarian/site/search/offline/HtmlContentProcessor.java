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

import java.io.FileInputStream;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlContentProcessor extends AbstractHtmlProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(HtmlContentProcessor.class);


	@Override
	public void process(Document document)
	{
		if(isHtml(document) && !document.hasRaw()) {
			org.jsoup.nodes.Document html;
			try {
				html = Jsoup.parse(new FileInputStream(document.file().toFile()), UTF_8.toString(), "");
				assignFields(document, html);
			} catch(IOException e) {
				LOG.warn("Failed to process HTML {} due to: {}", document.file(), e.getMessage());
				LOG.debug("Failed to process HTML", e);
			}
		}
	}


	private void assignFields(Document document, org.jsoup.nodes.Document html)
	{
		document.field(Document.TITLE, html.getElementsByTag("title").text());
		document.field(Document.DESCRIPTION, html.getElementsByTag("meta").select("[name=description]").attr("content"));

		document.field(Document.RAW_TEXT, html.getElementsByTag("h1").text() + ' ' +
				html.getElementsByTag("h2").text() + ' ' +
				html.getElementsByTag("h3").text() + ' ' +
				html.getElementsByTag("h4").text() + ' ' +
				html.getElementsByTag("h5").text() + ' ' +
				html.getElementsByTag("h6").text() + ' ' +
				html.getElementsByTag("p").text());
	}
}
