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
import java.io.OutputStream;
import java.util.Objects;

import javax.annotation.WillCloseWhenClosed;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class CountedOutputStream extends OutputStream {

	private final OutputStream delegate;
	private long count;


	public CountedOutputStream(@WillCloseWhenClosed OutputStream wrapped)
	{
		Objects.requireNonNull(wrapped);
		this.delegate = wrapped;
	}


	@Override
	public void close() throws IOException
	{
		delegate.close();
	}


	public long count()
	{
		return count;
	}


	public void resetCount()
	{
		count = 0L;
	}


	@Override
	public void write(int b) throws IOException
	{
		delegate.write(b);
		++count;
	}


	@Override
	public void write(byte[] b) throws IOException
	{
		delegate.write(b);
		count += b.length;
	}


	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		delegate.write(b, off, len);
		count += len;
	}
}
