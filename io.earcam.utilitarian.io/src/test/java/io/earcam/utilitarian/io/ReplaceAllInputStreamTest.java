/*-
 * #%L
 * io.earcam.utilitarian.io
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
package io.earcam.utilitarian.io;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class ReplaceAllInputStreamTest {

	@Test
	public void replaceSingleOccurrence() throws IOException
	{
		byte[] in = bytes("Some sample text");

		byte[] search = bytes("sample");
		byte[] replace = bytes("example");

		try(ReplaceAllInputStream input = new ReplaceAllInputStream(search, replace, wrapInput(in))) {
			String out = readAll(input);

			assertThat(out, is(equalTo("Some example text")));
		}
	}


	private MarkSupportedInputStream wrapInput(byte[] in)
	{
		return new MarkSupportedInputStream(new ByteArrayInputStream(in));
	}


	@Test
	public void replaceSingleOccurrenceAtStart() throws IOException
	{
		byte[] in = bytes("So many samples");

		byte[] search = bytes("So many");
		byte[] replace = bytes("Too many");

		try(ReplaceAllInputStream input = new ReplaceAllInputStream(search, replace, wrapInput(in))) {
			String out = readAll(input);

			assertThat(out, is(equalTo("Too many samples")));
		}
	}


	@Test
	public void replaceSingleOccurrenceAtEnd() throws IOException
	{
		byte[] in = bytes("Audio samples");

		byte[] search = bytes("samples");
		byte[] replace = bytes("cassette");

		try(ReplaceAllInputStream input = new ReplaceAllInputStream(search, replace, wrapInput(in))) {
			String out = readAll(input);

			assertThat(out, is(equalTo("Audio cassette")));
		}
	}


	@Test
	public void falseStartNoOccurrences() throws IOException
	{
		byte[] in = bytes("same text");

		byte[] search = bytes("sample");
		byte[] replace = bytes("example");

		try(ReplaceAllInputStream input = new ReplaceAllInputStream(search, replace, wrapInput(in))) {
			String out = readAll(input);

			assertThat(out, is(equalTo("same text")));
		}
	}


	private static byte[] bytes(String text)
	{
		return text.getBytes(UTF_8);
	}


	private String readAll(InputStream input) throws IOException
	{
		return new String(readAllBytes(input), UTF_8);
	}


	private byte[] readAllBytes(InputStream input) throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int b;
		while((b = input.read()) != -1) {
			output.write(b);
		}
		return output.toByteArray();
	}


	// EARCAM_SNIPPET_BEGIN: ReplaceAllInputStream
	@Test
	public void replaceMutlipleOccurrences() throws IOException
	{
		byte[] in = bytes("Some sample text, sampled by some for sample's sake");

		byte[] search = bytes("sample");
		byte[] replace = bytes("example");

		try(ReplaceAllInputStream input = new ReplaceAllInputStream(search, replace, wrapInput(in))) {
			String out = readAll(input);

			assertThat(out, is(equalTo("Some example text, exampled by some for example's sake")));
		}
	}
	// EARCAM_SNIPPET_END: ReplaceAllInputStream


	@Test
	public void mutlipleOccurrencesWithShorterReplacementThanSearch() throws IOException
	{
		byte[] in = bytes("Some sample text, sampled by some for sample's sake");

		byte[] search = bytes("sample");
		byte[] replace = bytes("ample");

		try(ReplaceAllInputStream input = new ReplaceAllInputStream(search, replace, wrapInput(in))) {
			String out = readAll(input);

			assertThat(out, is(equalTo("Some ample text, ampled by some for ample's sake")));
		}
	}


	@Test
	public void deleteMutlipleOccurrences() throws IOException
	{
		byte[] in = bytes("Some sample text, sampled by some for sample's sake");

		byte[] search = bytes(" sample");
		byte[] replace = new byte[0];

		try(ReplaceAllInputStream input = new ReplaceAllInputStream(search, replace, wrapInput(in))) {
			String out = readAll(input);

			assertThat(out, is(equalTo("Some text,d by some for's sake")));
		}
	}
}
