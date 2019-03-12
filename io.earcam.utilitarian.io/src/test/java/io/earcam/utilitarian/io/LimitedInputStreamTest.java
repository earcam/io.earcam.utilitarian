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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

public class LimitedInputStreamTest {

	@Test
	public void doesNotCloseUnderlying() throws Exception
	{
		AtomicBoolean closed = new AtomicBoolean(false);
		InputStream input = new InputStream() {

			@Override
			public int read() throws IOException
			{
				return 0;
			}


			@Override
			public void close() throws IOException
			{
				closed.set(true);
			}
		};

		try(LimitedInputStream limited = new LimitedInputStream(input, 5)) {
			assertThat(closed.get(), is(false));
		}
		assertThat(closed.get(), is(false));
	}


	@Test
	public void withinLimit() throws Exception
	{
		ByteArrayInputStream input = new ByteArrayInputStream(bytes("0123456789"));

		@SuppressWarnings("resource")
		LimitedInputStream limited = new LimitedInputStream(input, 5);

		byte[] buffer = new byte[5];
		int read = limited.read(buffer);

		assertThat(read, is(5));
		assertThat(new String(buffer, 0, read, UTF_8), is(equalTo("01234")));
	}


	private static byte[] bytes(String text)
	{
		return text.getBytes(UTF_8);
	}


	@Test
	public void exceedsLimit() throws Exception
	{
		ByteArrayInputStream input = new ByteArrayInputStream(bytes("0123456789"));

		@SuppressWarnings("resource")
		LimitedInputStream limited = new LimitedInputStream(input, 6);

		byte[] buffer = new byte[10];
		int read = limited.read(buffer);

		assertThat(read, is(6));
		assertThat(new String(buffer, 0, read, UTF_8), is(equalTo("012345")));
	}


	@Test
	public void readBytePastLimitReturnsMinusOne() throws Exception
	{
		ByteArrayInputStream input = new ByteArrayInputStream(bytes("xyz"));
		int x = 'x';

		@SuppressWarnings("resource")
		LimitedInputStream limited = new LimitedInputStream(input, 1);

		assertThat(limited.read(), is(x));
		assertThat(limited.read(), is(-1));
	}


	@Test
	public void readByteArrayPastLimitReturnsMinusOne() throws Exception
	{
		ByteArrayInputStream input = new ByteArrayInputStream(bytes("xyz"));

		byte[] buffer = new byte[4];

		@SuppressWarnings("resource")
		LimitedInputStream limited = new LimitedInputStream(input, 2);

		int read = limited.read(buffer);
		assertThat(read, is(2));
		assertThat(limited.read(buffer), is(-1));
	}


	@Test
	public void skips() throws Exception
	{
		ByteArrayInputStream input = new ByteArrayInputStream(bytes("0123456789"));

		@SuppressWarnings("resource")
		LimitedInputStream limited = new LimitedInputStream(input, 6);

		assertThat(limited.read(), is(48 + 0));
		assertThat(limited.read(), is(48 + 1));

		long skipped = limited.skip(2);
		assertThat(skipped, is(2L));

		assertThat(limited.read(), is(48 + 4));

		skipped = limited.skip(5);
		assertThat(skipped, is(1L));
	}
}
