/*-
 * #%L
 * io.earcam.utilitarian.file
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
import java.io.OutputStream;

/**
 * A search and replace filtering {@link OutputStream} wrapper
 *
 * @see ReplaceAllInputStream
 */
@SuppressWarnings("squid:S4349") // Sonar: Not applicable IMO
public final class ReplaceAllOutputStream extends OutputStream {

	private final byte[] search;
	private final byte[] replace;
	private final OutputStream wrapped;
	private volatile int position;


	/**
	 * Create a new {@link ReplaceAllOutputStream}
	 *
	 * @param search the byte sequence to search for
	 * @param replace the replacement byte sequence to substitute when the {@code search} sequence if found
	 * @param output the {@link OutputStream} to operate on
	 */
	public ReplaceAllOutputStream(byte[] search, byte[] replace, OutputStream output)
	{
		this.search = search;
		this.replace = replace;
		this.wrapped = output;
		this.position = 0;
	}


	@Override
	public void write(int b) throws IOException
	{
		if(position == search.length) {
			wrapped.write(replace);
			position = 0;
		}
		if(search[position] == b) {
			++position;
		} else {
			wrapped.write(search, 0, position);
			wrapped.write(b);
			position = 0;
		}
	}


	@Override
	public void close() throws IOException
	{
		if(position > 0) {
			wrapped.write(search, 0, position);
		}
		wrapped.close();
	}
}
