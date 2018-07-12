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

import static io.earcam.utilitarian.io.MarkSupportedInputStream.ensureMarkSupported;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * TODO tests
 */
public class MarkSupportedInputStreamTest {

	public final byte[] bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".getBytes(UTF_8);


	@Test
	public void markAndResetWithinLimit() throws Exception
	{
		byte[] buffer = new byte[26];
		try(MarkSupportedInputStream input = new MarkSupportedInputStream(new ByteArrayInputStream(bytes))) {

			input.read(buffer);
			input.mark(26);

			char c = (char) input.read();
			assertThat(c, is('a'));

			input.read(buffer, 0, 25);
			assertThat((char) buffer[24], is('z'));

			input.reset();

			c = (char) input.read();
			assertThat(c, is('a'));
		}
	}


	@Test
	public void markExceedsLimit() throws Exception
	{
		byte[] buffer = new byte[26];
		try(MarkSupportedInputStream input = new MarkSupportedInputStream(new ByteArrayInputStream(bytes))) {

			input.read(buffer);
			input.mark(26);

			char c = (char) input.read();
			assertThat(c, is('a'));

			input.read(buffer);
			assertThat((char) buffer[25], is('0'));

			c = (char) input.read();
			assertThat(c, is('1'));
		}
	}


	@Test
	public void whenMarkExceedsLimitThenResetThrows() throws Exception
	{
		byte[] buffer = new byte[26];
		try(MarkSupportedInputStream input = new MarkSupportedInputStream(new ByteArrayInputStream(bytes))) {

			input.read(buffer);
			input.mark(10);

			input.read(buffer);

			try {
				input.reset();
				fail();
			} catch(IOException ioe) {}
		}
	}


	@Test
	public void increasingReadLimitMarksWithinReset() throws Exception
	{
		byte[] buffer = new byte[26];
		try(MarkSupportedInputStream input = new MarkSupportedInputStream(new ByteArrayInputStream(bytes))) {
			input.mark(10);
			input.read(buffer, 0, 10);

			assertThat((char) buffer[9], is('J'));
			input.reset();

			input.mark(27);
			input.read(buffer);

			assertThat((char) buffer[0], is('A'));
			assertThat((char) buffer[21], is('V'));
			input.reset();
			assertThat((char) input.read(), is('A'));
		}
	}


	@Test
	public void resetThrowsWhenNotAlreadyMarked()
	{
		try(MarkSupportedInputStream input = new MarkSupportedInputStream(new ByteArrayInputStream("foo".getBytes(UTF_8)))) {
			input.reset();
			fail();
		} catch(IOException ioe) {}
	}


	@Test
	public void ensureMarkSupportedWrapsWhenUnderlyingDoesNotSupportMarking()
	{
		InputStream input = new ByteArrayInputStream(new byte[0]) {
			@Override
			public boolean markSupported()
			{
				return false;
			}
		};
		assertThat(ensureMarkSupported(input), is(instanceOf(MarkSupportedInputStream.class)));
	}


	@Test
	public void ensureMarkSupportedReturnsOriginalWhenOriginalSupportsMarking()
	{
		InputStream input = new ByteArrayInputStream(new byte[0]) {
			@Override
			public boolean markSupported()
			{
				return true;
			}
		};
		assertThat(ensureMarkSupported(input), is(sameInstance(input)));
	}
}
