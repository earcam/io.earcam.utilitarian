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
package io.earcam.utilitarian.file;

import static java.util.Locale.ROOT;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;

public class SplitFileSuffix implements IntFunction<String>, Iterator<String> {

	private int width;
	private int total;
	private int position;


	private SplitFileSuffix(int total, int width)
	{
		this.total = total;
		this.width = width;
	}


	public static SplitFileSuffix splitFileSuffix(Collection<?> splits)
	{
		return splitFileSuffix(splits.size());
	}


	public static SplitFileSuffix splitFileSuffix(int total)
	{
		if(total < 0) {
			throw new IllegalArgumentException("total cannot be negative, received: " + total);
		}
		return new SplitFileSuffix(total, width(total));
	}


	private static int width(int total)
	{
		int multiple = 1;
		int c = 0;
		while(total >= multiple) {
			multiple *= 10;
			++c;
		}
		return c;
	}


	@Override
	public String apply(int index)
	{
		if(index >= total || index < 0) {
			throw new IndexOutOfBoundsException("total: " + total + ", index: " + index);
		}
		if(total == 1 && index == 0) {
			return "";
		}
		String format = "%0" + width + "d";
		return String.format(ROOT, format, index + 1);  //TODO populate and return a char array instead 
	}


	@Override
	public boolean hasNext()
	{
		return position < total;
	}


	@Override
	public String next()
	{
		if(!hasNext()) {
			throw new NoSuchElementException();
		}
		return apply(position++);
	}
}
