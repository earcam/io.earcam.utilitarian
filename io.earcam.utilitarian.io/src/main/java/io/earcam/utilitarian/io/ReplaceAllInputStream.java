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

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.WillCloseWhenClosed;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * <p>
 * A search and replace filtering {@link InputStream} wrapper.
 * </p>
 *
 * <p>
 * Requires the underlying {@link InputStream} supports mark and reset; so uses
 * {@link MarkSupportedInputStream#ensureMarkSupported(InputStream)} to guarantee
 * this prerequisite.
 * </p>
 *
 * @see ReplaceAllOutputStream
 */
@NotThreadSafe
public final class ReplaceAllInputStream extends InputStream {

	private static final int UNPOSITIONED = -1;
	private final byte[] search;
	private final byte[] replace;
	private final InputStream wrapped;
	private int position = UNPOSITIONED;


	/**
	 * Create a new {@link ReplaceAllInputStream}
	 *
	 * @param search the byte sequence to search for
	 * @param replace the replacement byte sequence to substitute when the {@code search} sequence if found
	 * @param input the {@link InputStream} to operate on
	 */
	public ReplaceAllInputStream(byte[] search, byte[] replace, @WillCloseWhenClosed InputStream input)
	{
		this.search = search;
		this.replace = replace;
		this.wrapped = ensureMarkSupported(input);
	}


	@Override
	public int read() throws IOException
	{
		if(position != UNPOSITIONED && position < replace.length) {
			return replace[position++] & 0xFF;
		}
		int read = wrapped.read();
		if(read == search[0]) {
			wrapped.mark(search.length);
			int p = search();
			if(p == search.length) {
				position = 1;
				return replace.length == 0 ? read() : (replace[0] & 0xFF);
			} else {
				wrapped.reset();
			}
		}
		position = UNPOSITIONED;
		return read;
	}


	private int search() throws IOException
	{
		int p = 1;
		while(p < search.length && wrapped.read() == search[p]) {
			++p;
		}
		return p;
	}


	@Override
	public void close() throws IOException
	{
		wrapped.close();
	}
}