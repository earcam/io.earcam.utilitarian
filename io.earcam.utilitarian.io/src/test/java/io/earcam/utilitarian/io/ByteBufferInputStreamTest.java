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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ByteBufferInputStreamTest {

	@Test
	public void bufferIsReadWithoutOriginalPositionChanging()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		byte[] read = new byte[4];
		try(ByteBufferInputStream input = new ByteBufferInputStream(buffer)) {
			input.read(read);
		}

		assertThat(read, is(equalTo(new byte[] { 0, 2, 4, 8 })));
		assertThat(buffer.position(), is(equalTo(0)));
	}


	@Test
	public void markIsSupported()
	{
		try(ByteBufferInputStream input = new ByteBufferInputStream(ByteBuffer.wrap(new byte[42]))) {
			assertThat(input.markSupported(), is(true));
		}
	}


	@Test
	public void markSupported()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(ByteBufferInputStream input = new ByteBufferInputStream(buffer)) {
			input.read();
			input.read();
			input.mark(1_000);
			input.read();
			input.reset();
			int read = input.read();

			assertThat(read, is(0b100));
		}
	}


	@Test
	public void whenDuplicatedThenBufferPositionDoesNotMoveWithStream()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(ByteBufferInputStream input = new ByteBufferInputStream(buffer, true)) {
			input.skip(5L);
			input.read();

			assertThat(buffer.position(), is(0));
		}
	}


	@Test
	public void whenNotDuplicatedThenBufferPositionMovesWithStream()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(ByteBufferInputStream input = new ByteBufferInputStream(buffer, false)) {
			input.skip(5L);
			input.read();

			assertThat(buffer.position(), is(6));
		}
	}


	@Test
	public void skips()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(ByteBufferInputStream input = new ByteBufferInputStream(buffer)) {
			long skipped = input.skip(5L);
			int read = input.read();

			assertThat(skipped, is(5L));
			assertThat(read, is(0b100000));
		}
	}


	@Test
	public void whenSkipIsZeroThenZeroSkipped()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(ByteBufferInputStream input = new ByteBufferInputStream(buffer)) {
			input.skip(0L);
			int read = input.read();

			assertThat(read, is(0));
		}
	}


	@Test
	public void whenSkipLessThanZeroThenZeroSkipped()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(ByteBufferInputStream input = new ByteBufferInputStream(buffer)) {
			input.skip(-5L);
			int read = input.read();

			assertThat(read, is(0));
		}
	}


	@Test
	public void readPastEndReturnsMinusOne()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0b1, 0b10, 0b100 });
		try(ByteBufferInputStream input = new ByteBufferInputStream(buffer)) {
			input.skip(4L);
			int read = input.read();

			assertThat(read, is(-1));
		}
	}


	@Test
	public void readsPastEndReturnsMinusOne()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0b1, 0b10, 0b100 });
		try(ByteBufferInputStream input = new ByteBufferInputStream(buffer)) {
			input.skip(4L);
			int read = input.read(new byte[32]);

			assertThat(read, is(-1));
		}
	}


	@Test
	public void readReturnBytesRead()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0b1, 0b10, 0b100 });
		try(ByteBufferInputStream input = new ByteBufferInputStream(buffer)) {
			int read = input.read(new byte[32]);

			assertThat(read, is(3));
		}
	}
}
