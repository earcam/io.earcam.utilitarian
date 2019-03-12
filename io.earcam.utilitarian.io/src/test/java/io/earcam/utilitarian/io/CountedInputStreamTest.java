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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

public class CountedInputStreamTest {

	@Test
	public void underlyingIsClosed() throws IOException
	{
		AtomicBoolean wasClosed = new AtomicBoolean();

		InputStream stub = new InputStream() {

			@Override
			public int read()
			{
				return 0;
			}


			@Override
			public void close()
			{
				wasClosed.set(true);
			}
		};
		try(CountedInputStream input = new CountedInputStream(stub)) {

		}
		assertThat(wasClosed.get(), is(true));
	}


	@Test
	public void requireThatWrappedInputStreamIsNotNull() throws IOException
	{
		try(InputStream varWillNotBeAssigned = new CountedInputStream(null)) {
			fail();
		} catch(NullPointerException e) {}
	}


	@Test
	public void whenUninitializedThenTheCountIsZero() throws IOException
	{
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(new byte[10]))) {
			assertThat(input.count(), is(0L));
		}
	}


	@Test
	public void afterReadingOneByteThenCountIsOne() throws IOException
	{
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(new byte[1]))) {
			input.read();
			assertThat(input.count(), is(1L));
		}
	}


	@Test
	public void endOfStreamIsHonoured() throws IOException
	{
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(new byte[1]))) {
			input.read();
			assertThat(input.read(), is(-1));
		}
	}


	@Test
	public void countNotChangedByRepeatedReadsPastEndOfStream() throws IOException
	{
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(new byte[2]))) {
			input.read();
			input.read();
			assertThat(input.read(), is(-1));
			input.read();
			input.read();
			assertThat(input.count(), is(2L));
		}
	}


	@Test
	public void countNotChangedByRepeatedArrayReadsPastEndOfStream() throws IOException
	{
		byte[] buffer = new byte[2];
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(new byte[3]))) {
			input.read(buffer);
			assertThat(input.read(buffer), is(1));
			input.read(buffer);
			input.read(buffer);
			assertThat(input.count(), is(3L));
		}
	}


	@Test
	public void afterThreeCallsToReadTheCountIsThree() throws IOException
	{
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(new byte[4]))) {
			input.read();
			input.read();
			input.read();
			// Why three? https://www.youtube.com/watch?v=aU4pyiB-kq0 or https://www.youtube.com/watch?v=dLBx3g8cowY
			assertThat(input.count(), is(3L));
		}
	}


	@Test
	public void byteReturnedFromReadIsEqualToThatFromDelegate() throws IOException
	{
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(new byte[] { 42 }))) {
			assertThat(input.read(), is(42));
		}
	}


	@Test
	public void byteArrayPopulatedByReadIsEqualToThatFromDelegate() throws IOException
	{
		byte[] bytes = new byte[] { 0, 2, 4, 8, 16, 32, 64 };
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(bytes))) {
			byte[] buffer = new byte[3];
			int read = input.read(buffer);

			assertThat(read, is(3));
			assertThat(buffer, is(equalTo(new byte[] { bytes[0], bytes[1], bytes[2] })));
			assertThat(input.count(), is(3L));
		}
	}


	@Test
	public void callingResetSetsCountToZero() throws IOException
	{
		byte[] bytes = new byte[] { 0, 2, 4, 8, 16, 32, 64 };
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(bytes))) {
			input.read();
			input.read();
			input.read();
			input.read();

			input.resetCount();

			assertThat(input.count(), is(0L));
		}
	}


	@Test
	public void markSupported() throws IOException
	{
		byte[] bytes = new byte[] { 0, 2, 4, 8, 16, 32, 64 };
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(bytes))) {
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
	public void countCorrectWhenMarkedThenMarkReset() throws IOException
	{
		byte[] bytes = new byte[] { 0, 2, 4, 8, 16, 32, 64 };
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(bytes))) {
			input.read();
			input.read();
			input.mark(1_000);
			input.read();
			input.reset();
			input.read();

			assertThat(input.count(), is(3L));
		}
	}


	@Test
	public void countCorrectWhenMarkedThenMarkResetAndCountReset() throws IOException
	{
		byte[] bytes = new byte[] { 0, 2, 4, 8, 16, 32, 64 };
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(bytes))) {
			input.read();
			input.read();
			input.mark(1_000);
			input.read();
			input.reset();          // genuinely read 3 bytes
			input.read();           // re-read 3rd byte
			input.resetCount();     // now "level"
			input.read();

			assertThat(input.count(), is(1L));
		}
	}


	@SuppressWarnings("resource")
	@Test
	public void availableIsDelegated() throws IOException
	{
		InputStream delegate = new InputStream() {

			@Override
			public int read()
			{
				return 0;
			}


			@Override
			public int available()
			{
				return 42;
			}
		};

		assertThat(new CountedInputStream(delegate).available(), is(42));
	}


	@Test
	public void countCorrectWhenMarkedThenSkippedAndMarkReset() throws IOException
	{
		byte[] bytes = "it's the end of the world as we know it".getBytes(UTF_8);
		try(CountedInputStream input = new CountedInputStream(new ByteArrayInputStream(bytes))) {

			input.read(new byte[5]);
			input.mark(1_000);
			input.skip(10);
			input.read(new byte[15]);
			input.reset();

			assertThat(input.count(), is(5L));
		}
	}
}
