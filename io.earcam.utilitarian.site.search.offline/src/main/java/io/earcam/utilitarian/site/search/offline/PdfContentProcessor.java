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

import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.earcam.unexceptional.Closing;

public class PdfContentProcessor implements Processor {

	private static final Logger LOG = LoggerFactory.getLogger(PdfContentProcessor.class);


	@Override
	public void process(Document document)
	{
		if(isPdf(document) && !document.hasRaw()) {

			try {
				Closing.closeAfterAccepting(PDDocument::load, document.file().toFile(), document, this::consume);
			} catch(UncheckedIOException e) {
				LOG.warn("Failed to process PDF {} due to: {}", document.file(), e.getMessage());
				LOG.debug("Failed to process PDF", e.getCause());
			}
		}
	}


	private void consume(PDDocument pdf, Document document) throws IOException
	{
		PDDocumentInformation information = pdf.getDocumentInformation();
		document.field(Document.TITLE, information.getTitle());

		PDFTextStripper stripper = new PDFTextStripper();
		String text = stripper.getText(pdf);
		document.field(Document.RAW_TEXT, text);
	}


	private boolean isPdf(Document document)
	{
		return "application/pdf".equals(document.contentType());
	}
}
