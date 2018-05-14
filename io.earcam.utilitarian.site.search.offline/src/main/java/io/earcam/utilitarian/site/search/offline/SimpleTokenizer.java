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

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.earcam.unexceptional.Closing;

public class SimpleTokenizer implements Processor {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleTokenizer.class);


	public List<String> tokenize(String input)
	{
		try {
			return Closing.closeAfterApplying(createAnalyzer(), input, this::tokens);
		} catch(UncheckedIOException e) {
			LOG.warn("Failed to tokenize '{}', due to {}", input, e.getMessage());
			LOG.debug("Failed to tokenize", e.getCause());
		}
		return emptyList();
	}


	private List<String> tokens(Analyzer analyzer, String input)
	{
		return Closing.closeAfterApplying(analyzer.tokenStream(null, new StringReader(input)), this::streamTokens);
	}


	private List<String> streamTokens(TokenStream stream) throws IOException
	{
		stream.reset();
		List<String> tokens = new ArrayList<>();
		while(stream.incrementToken()) {
			tokens.add(stream.getAttribute(CharTermAttribute.class).toString());
		}
		return tokens;
	}


	/**
	 * <p>
	 * Override this method to return a custom {@link Analyzer}.
	 * </p>
	 * <p>
	 * Note; Use of Lucene for stemming, stopword filtering, etc must match
	 * whatever is configured for lunrjs.
	 * </p>
	 *
	 * @return an {@link Analyzer} for tokenizing
	 */
	protected Analyzer createAnalyzer()
	{
		return new SimpleAnalyzer();
	}


	@Override
	public void process(Document document)
	{
		if(document.hasRaw() && !document.hasTokens()) {
			List<String> tokenized = tokenize(document.raw());
			document.tokens().addAll(tokenized);
		}
	}
}
