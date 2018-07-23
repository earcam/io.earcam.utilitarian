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

import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

	private final ByteBuffer buffer;


	public ByteBufferInputStream(ByteBuffer buffer)
	{
		this(buffer, true);
	}


	public ByteBufferInputStream(ByteBuffer buffer, boolean duplicate)
	{
		this.buffer = duplicate ? buffer.duplicate() : buffer;
	}


	@Override
	public int read()
	{
		if(available() == 0) {
			return -1;
		}
		return 0xFF & buffer.get();
	}


	@Override
	public int read(byte[] b)
	{
		return read(b, 0, b.length);
	}


	@Override
	public int read(byte[] b, int off, int len)
	{
		int remaining = available();
		if(remaining == 0) {
			return -1;
		}
		int length = Math.min(len, remaining);
		buffer.get(b, off, length);
		return length;
	}


	@Override
	public long skip(long n)
	{
		if(n <= 0L) {
			return 0L;
		}
		int skipped = Math.min((int) n, buffer.remaining());
		buffer.position(buffer.position() + skipped);
		return skipped;
	}


	@Override
	public int available()
	{
		return buffer.remaining();
	}


	@Override
	public void mark(int readlimit)
	{
		buffer.mark();
	}


	@Override
	public void reset()
	{
		buffer.reset();
	}


	@Override
	public boolean markSupported()
	{
		return true;
	}


	@Override
	public void close()
	{
		/* NoOp */
	}
}
