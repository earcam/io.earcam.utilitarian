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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

public class ReplaceAllOutputStreamTest {

	@Test
	public void replaceSingleOccurrence() throws Exception
	{
		byte[] out = bytes("Some example text");

		byte[] search = bytes("example");
		byte[] replace = bytes("sample");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(ReplaceAllOutputStream output = new ReplaceAllOutputStream(search, replace, baos)) {
			output.write(out);
		}
		Assert.assertThat(text(baos.toByteArray()), is(equalTo("Some sample text")));
	}


	private String text(byte[] bytes)
	{
		return new String(bytes, UTF_8);
	}


	private static byte[] bytes(String text)
	{
		return text.getBytes(UTF_8);
	}


	// EARCAM_SNIPPET_BEGIN: ReplaceAllOutputStream
	@Test
	public void replaceConsecutiveOccurrences() throws Exception
	{
		byte[] out = bytes("ample samplesamplesample samples");

		byte[] search = bytes("sample");
		byte[] replace = bytes("example");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(ReplaceAllOutputStream output = new ReplaceAllOutputStream(search, replace, baos)) {
			output.write(out);
		}
		Assert.assertThat(text(baos.toByteArray()), is(equalTo("ample exampleexampleexample examples")));
	}
	// EARCAM_SNIPPET_BEGIN: ReplaceAllOutputStream


	@Test
	public void mutlipleOccurrencesWithShorterReplacementThanSearch() throws Exception
	{
		byte[] out = bytes("ample sampled samples sampled");

		byte[] search = bytes(" sample");
		byte[] replace = new byte[0];

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(ReplaceAllOutputStream output = new ReplaceAllOutputStream(search, replace, baos)) {
			output.write(out);
		}
		Assert.assertThat(text(baos.toByteArray()), is(equalTo("ampledsd")));
	}


	@Test
	public void whenClosedThenClosesUnderlying() throws IOException
	{
		AtomicBoolean closed = new AtomicBoolean(false);
		OutputStream dummy = new OutputStream() {

			@Override
			public void write(int b) throws IOException
			{}


			@Override
			public void close() throws IOException
			{
				closed.set(true);
			}
		};
		try(ReplaceAllOutputStream output = new ReplaceAllOutputStream(new byte[1], new byte[1], dummy)) {}
		assertThat(closed.get(), is(true));
	}
}
