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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * <p>
 * Wraps an {@link InputStream} to ensure {@link InputStream#markSupported()} returns {@code true}.
 * </p>
 *
 *
 * <p>
 * <b>Note:</b> if calls to {@link #read()} exceed the {@code readLimit} parameter of {@link #mark(int)}
 * then the mark is removed and a call to {@link #reset()} will throw an {@link IOException}.
 * </p>
 *
 */
public final class MarkSupportedInputStream extends InputStream {

	private volatile int[] buffer = new int[0];
	private volatile int readPosition = 0;
	private volatile int writePosition = 0;
	private volatile int readLimit = 0;
	private final InputStream delegate;


	/**
	 * Create an InputStream with mark supported
	 *
	 * @param delegate the {@link InputStream} to wrap
	 */
	public MarkSupportedInputStream(InputStream delegate)
	{
		this.delegate = delegate;
	}


	/**
	 * For efficient use of memory, this convenience static method
	 * returns the {@code input} argument IFF it claims to to support marking,
	 * otherwise the stream is wrapped
	 *
	 * @param input the {@link InputStream} to check
	 * @return an {@link InputStream} that supports marking
	 */
	public static InputStream ensureMarkSupported(InputStream input)
	{
		return input.markSupported() ? input : new MarkSupportedInputStream(input);
	}


	@Override
	public int read() throws IOException
	{
		if(writePosition == readLimit) {
			readPosition = writePosition = readLimit = 0;
			return delegate.read();
		}
		if(readPosition < writePosition) {
			return buffer[readPosition++];
		}
		int read = delegate.read();
		buffer[writePosition] = read;
		writePosition++;
		readPosition++;
		return read;
	}


	@Override
	public final boolean markSupported()
	{
		return true;
	}


	@Override
	public synchronized void mark(int readLimit)
	{
		this.readLimit = readLimit + 1;
		if(buffer.length < this.readLimit || readPosition > 0) {
			buffer = Arrays.copyOfRange(buffer, readPosition, readPosition + this.readLimit);
			writePosition -= readPosition;
		}
		readPosition = 0;
	}


	@Override
	public synchronized void reset() throws IOException
	{
		if(readLimit == 0) {
			throw new IOException("Not marked, or current position > marked position + readLimit");
		}
		readPosition = 0;
	}
}
