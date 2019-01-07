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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

public class CountedOutputStreamTest {

	@Test
	public void closesDelegateWhenClosed() throws IOException
	{
		AtomicBoolean closed = new AtomicBoolean(false);
		ByteArrayOutputStream delegate = new ByteArrayOutputStream() {
			public void close()
			{
				closed.set(true);
			}
		};

		try(CountedOutputStream output = new CountedOutputStream(delegate)) {}

		assertThat(closed.get(), is(true));
	}


	@Test
	public void count() throws IOException
	{
		ByteArrayOutputStream delegate = new ByteArrayOutputStream();

		try(CountedOutputStream output = new CountedOutputStream(delegate)) {

			output.write('1');
			output.write("2345".getBytes(UTF_8));
			output.write("123456789".getBytes(UTF_8), 5, 4);

			assertThat(output.count(), is(9L));
		}
	}


	@Test
	public void reset() throws IOException
	{
		ByteArrayOutputStream delegate = new ByteArrayOutputStream();

		try(CountedOutputStream output = new CountedOutputStream(delegate)) {

			output.write("12345".getBytes(UTF_8));
			output.resetCount();
			output.write('6');
			assertThat(output.count(), is(1L));
		}
	}
}
