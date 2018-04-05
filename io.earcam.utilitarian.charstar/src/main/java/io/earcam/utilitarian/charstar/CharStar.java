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
package io.earcam.utilitarian.charstar;

import java.util.Arrays;

public class CharStar implements CharSequence {

	private final char[] sequence;
	private final int offset;
	private final int length;


	private CharStar(char[] sequence)
	{
		this(sequence, 0, sequence.length);
	}


	private CharStar(char[] sequence, int offset, int length)
	{
		this.sequence = sequence;
		this.offset = offset;
		this.length = length;
	}


	public static CharStar charSequence(char[] sequence)
	{
		return new CharStar(sequence);
	}


	public static CharStar immutableCharSequence(char[] sequence)
	{
		char[] encapsulated = Arrays.copyOf(sequence, sequence.length);
		return new CharStar(encapsulated);
	}


	@Override
	public boolean equals(Object other)
	{
		return other instanceof CharStar && equals((CharStar) other);
	}


	public boolean equals(CharStar that)
	{
		return that != null && CharSequences.same(this, that);
	}


	@Override
	public int hashCode()
	{
		return CharSequences.hashCode(this);
	}


	@Override
	public String toString()
	{
		return String.valueOf(sequence);
	}


	@Override
	public int length()
	{
		return length;
	}


	@Override
	public char charAt(int index)
	{
		return sequence[offset + index];
	}


	@Override
	public CharSequence subSequence(int start, int end)
	{
		if(end >= length) {
			throw new IndexOutOfBoundsException("end >= length: " + end + " >= " + length); // TODO other validation
		}
		return new CharStar(sequence, start, end);
	}

}
