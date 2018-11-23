/*-
 * #%L
 * io.earcam.instrumental.io
 * %%
 * Copyright (C) 2018 earcam
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
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;

public class IoStreamsTest {

	@Test
	public void transfer()
	{
		byte[] bytes = new byte[] { 0b0, 0b1, 0b10, 0b11, 0b100 };
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		long transferred = IoStreams.transfer(in, out);

		assertThat(transferred, is(5L));
		assertThat(out.toByteArray(), is(equalTo(bytes)));
	}


	@Test
	public void readErrorRethrownAsUnchecked()
	{
		final IOException chuck = new IOException("Oh noes");

		InputStream in = new InputStream() {
			@Override
			public int read() throws IOException
			{
				throw chuck;
			}
		};
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			IoStreams.transfer(in, out);
			fail();
		} catch(UncheckedIOException e) {
			assertThat(e.getCause(), is(sameInstance(chuck)));
		}

	}


	@Test
	public void writeErrorRethrownAsUnchecked()
	{
		final IOException chuck = new IOException("Oh noes");

		ByteArrayInputStream in = new ByteArrayInputStream("hello".getBytes(UTF_8));
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException
			{
				throw chuck;
			}
		};

		try {
			IoStreams.transfer(in, out);
			fail();
		} catch(UncheckedIOException e) {
			assertThat(e.getCause(), is(sameInstance(chuck)));
		}

	}
}
