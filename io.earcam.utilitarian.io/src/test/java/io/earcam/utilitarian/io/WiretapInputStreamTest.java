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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class WiretapInputStreamTest {

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
		try(WiretapInputStream input = new WiretapInputStream(stub, true)) {}
		assertThat(wasClosed.get(), is(true));
	}


	@Test
	public void constructorArgumentDeterminesTapIsOn() throws Exception
	{
		try(WiretapInputStream input = new WiretapInputStream(new ByteArrayInputStream(new byte[0]), true)) {
			assertThat(input.tapping(), is(true));
		}
	}


	@Test
	public void constructorArgumentDeterminesTapIsOff() throws Exception
	{
		try(WiretapInputStream input = new WiretapInputStream(new ByteArrayInputStream(new byte[0]), false)) {
			assertThat(input.tapping(), is(false));
		}
	}


	@Test
	public void endOfStreamIsHonoured() throws IOException
	{
		try(WiretapInputStream input = new WiretapInputStream(new ByteArrayInputStream(new byte[1]), true)) {
			input.read();
			assertThat(input.read(), is(-1));
		}
	}


	@Test
	public void readOneByteWhenTapped() throws IOException
	{
		try(WiretapInputStream input = new WiretapInputStream(new ByteArrayInputStream(new byte[] { 42 }), true)) {
			int read = input.read();
			assertThat(read, is(42));
			assertThat(input.toByteArray(), is(equalTo(new byte[] { 42 })));
		}
	}


	@Test
	public void readOneByteWhenNotTapped() throws IOException
	{
		try(WiretapInputStream input = new WiretapInputStream(new ByteArrayInputStream(new byte[] { 42 }), false)) {
			int read = input.read();
			assertThat(read, is(42));
			assertThat(input.toByteArray(), is(equalTo(new byte[0])));
		}
	}


	@Test
	public void onlyStoresWhenTapOn() throws IOException
	{
		try(WiretapInputStream input = new WiretapInputStream(new ByteArrayInputStream(new byte[] { 0b0, 0b10, 0b100, 0b1000 }), false)) {
			input.read();
			input.tapOn();
			input.read();
			input.read();
			input.tapOff();
			input.read();
			assertThat(input.toByteArray(), is(equalTo(new byte[] { 0b10, 0b100 })));
		}
	}


	@Test
	public void tapsSkippedBytes() throws IOException
	{
		ByteArrayInputStream baos = new ByteArrayInputStream(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(WiretapInputStream input = new WiretapInputStream(baos, true)) {
			input.skip(5L);
			input.read();

			assertThat(input.toByteArray(), is(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000 }));
		}
	}


	@Test
	public void tapsSkippedBytesToEndOfStream() throws IOException
	{
		ByteArrayInputStream baos = new ByteArrayInputStream(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(WiretapInputStream input = new WiretapInputStream(baos, false)) {
			input.read();
			input.tapOn();
			input.skip(50L);

			assertThat(input.toByteArray(), is(new byte[] { 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 }));
		}
	}


	@Test
	public void bytesSkippedWhenTapOffAreNotStored() throws IOException
	{
		ByteArrayInputStream baos = new ByteArrayInputStream(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(WiretapInputStream input = new WiretapInputStream(baos, false)) {
			input.skip(5L);
			input.tapOn();
			input.read();
			input.read();

			assertThat(input.toByteArray(), is(new byte[] { 0b100000, 0b1000000 }));
		}
	}


	@Test
	public void tapNotAffectedByMarkAndReset() throws IOException
	{
		ByteArrayInputStream baos = new ByteArrayInputStream(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(WiretapInputStream input = new WiretapInputStream(baos, true)) {
			input.read();
			input.read();
			input.mark(1_000);
			input.read();
			input.read();
			input.reset();
			input.read();
			input.read();
			input.read();

			assertThat(input.toByteArray(), is(equalTo(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000 })));
		}
	}


	@Test
	public void tappingMayCommenceAfterMarkAndReset() throws IOException
	{
		ByteArrayInputStream baos = new ByteArrayInputStream(new byte[] { 0b0, 0b10, 0b100, 0b1000, 0b10000, 0b100000, 0b1000000 });
		try(WiretapInputStream input = new WiretapInputStream(baos, false)) {
			input.read();
			input.read();
			input.mark(1_000);
			input.read();
			input.tapOn();
			input.read();
			input.reset();
			input.read();
			input.read();
			input.read();

			assertThat(input.toByteArray(), is(equalTo(new byte[] { 0b1000, 0b10000 })));
		}
	}

}
