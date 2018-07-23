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

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class CountedInputStream extends InputStream {

	private final InputStream delegate;
	private long count;
	private boolean marked;
	private long mark;


	public CountedInputStream(InputStream wrapped)
	{
		Objects.requireNonNull(wrapped);
		this.delegate = wrapped;
	}


	@Override
	public void close() throws IOException
	{
		delegate.close();
	}


	public synchronized long count()
	{
		return count;
	}


	public synchronized void resetCount()
	{
		count = 0L;
	}


	@Override
	public synchronized void mark(int readlimit)
	{
		marked = true;
		delegate.mark(readlimit);
	}


	@Override
	public synchronized void reset() throws IOException
	{
		marked = false;
		delegate.reset();
	}


	@Override
	public int read() throws IOException
	{
		int read = delegate.read();
		if(read != -1) {
			if(marked) {
				++mark;
				++count;
			} else if(mark > 0) {
				--mark;
			} else {
				++count;
			}
		}
		return read;
	}
}
