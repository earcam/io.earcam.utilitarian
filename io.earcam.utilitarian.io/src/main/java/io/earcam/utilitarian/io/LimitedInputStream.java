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

import javax.annotation.WillNotClose;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class LimitedInputStream extends CountedInputStream {

	private long maximum;


	public LimitedInputStream(@WillNotClose InputStream wrapped, long maximum)
	{
		super(wrapped);
		this.maximum = maximum;
	}


	@Override
	public int read() throws IOException
	{
		return (unmarkedCount() >= maximum) ? -1 : super.read();
	}


	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		long remaining = maximum - unmarkedCount();
		if(len > remaining) {
			len = (int) remaining;
		}
		return (remaining == 0) ? -1 : super.read(b, off, len);
	}


	@Override
	public long skip(long n) throws IOException
	{
		if(unmarkedCount() + n <= maximum) {
			return super.skip(n);
		} else {
			return super.skip(maximum - unmarkedCount());
		}
	}


	@Override
	public void close() throws IOException
	{
		/* NOOP */
	}
}
