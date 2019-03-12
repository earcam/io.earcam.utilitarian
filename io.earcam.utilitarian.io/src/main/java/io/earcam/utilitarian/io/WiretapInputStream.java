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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.WillCloseWhenClosed;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class WiretapInputStream extends InputStream {

	private InputStream tapped;
	private ByteArrayOutputStream tap;
	private boolean tapping;
	private boolean marked;
	private int mark;


	public WiretapInputStream(@WillCloseWhenClosed InputStream tapped, boolean tapOn)
	{
		this.tapping = tapOn;
		this.tapped = tapped;
		this.tap = new ByteArrayOutputStream();
	}


	@Override
	public synchronized void mark(int readlimit)
	{
		marked = true;
		mark = 0;
		tapped.mark(readlimit);
	}


	@Override
	public synchronized void reset() throws IOException
	{
		marked = false;
		tapped.reset();
	}


	@Override
	public long skip(long n) throws IOException
	{
		if(tapping) {
			int i = 0;
			int b;
			while(i++ < n && (b = tapped.read()) != -1) {
				tap.write(b);
			}
			return i;
		}
		return super.skip(n);
	}


	@Override
	public void close() throws IOException
	{
		tapped.close();
	}


	@Override
	public int read() throws IOException
	{
		int read = tapped.read();
		if(marked) {
			++mark;
			if(tapping) {
				tap.write(read);
			}
		} else if(mark > 0) {
			--mark;
		} else if(tapping) {
			tap.write(read);
		}
		return read;
	}


	public byte[] toByteArray()
	{
		return tap.toByteArray();
	}


	public boolean tapping()
	{
		return tapping;
	}


	public void tapping(boolean tapOn)
	{
		tapping = tapOn;
	}


	public void tapOn()
	{
		tapping(true);
	}


	public void tapOff()
	{
		tapping(false);
	}
}
