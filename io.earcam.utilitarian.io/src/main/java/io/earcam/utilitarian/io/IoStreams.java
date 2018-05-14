/*-
 * #%L
 * io.earcam.instrumental.archive
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

import static io.earcam.unexceptional.Exceptional.accept;
import static io.earcam.unexceptional.Exceptional.get;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillNotClose;

@ParametersAreNonnullByDefault
public final class IoStreams {

	private IoStreams()
	{}


	/**
	 * While waiting for Java9's {@code InputStream.readAllBytes()}
	 * 
	 * @param input the stream to drain
	 * @return the byte array obtained from draining {@code input}
	 * @throws UncheckedIOException if an {@link IOException} this thrown
	 */
	public static byte[] readAllBytes(InputStream input)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		transfer(input, baos);
		return baos.toByteArray();
	}


	/**
	 * While waiting for Java9's {@code InputStream.transferTo(OutputStream)}
	 * 
	 * @param input read from
	 * @param output write to, but does not close
	 * @throws UncheckedIOException if an {@link IOException} this thrown
	 */
	public static long transfer(@WillNotClose InputStream in, @WillNotClose OutputStream out)
	{
		Objects.requireNonNull(in, "in");
		Objects.requireNonNull(out, "out");
		// TODO buffer this
		int b;
		while((b = get(in::read)) != -1) {
			accept(out::write, b);
		}
		return -1;
	}
}
