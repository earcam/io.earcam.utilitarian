/*-
 * #%L
 * io.earcam.utilitarian.charstar
 * %%
 * Copyright (C) 2017 - 2018 earcam
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

final class CharStar implements CharSequence, Comparable<CharSequence>, Externalizable {

	private static final long serialVersionUID = 42L;

	private char[] sequence;
	private int offset;
	private int length;


	public CharStar()
	{}


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


	static CharStar backedCharSequence(char[] sequence)
	{
		return new CharStar(sequence);
	}


	static CharStar charSequence(char[] sequence)
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
		return String.valueOf(sequence, offset, length);
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
	public CharStar subSequence(int start, int end)
	{
		requireValidIndices(start, end);
		return new CharStar(sequence, start, end - start);
	}


	private void requireValidIndices(int start, int end)
	{
		requireNonNegative("start", start);
		requireNonNegative("end", end);
		requireLessThanOrEqualTo("end", end, "length", length);
		requireLessThanOrEqualTo("start", start, "end", end);
	}


	private void requireNonNegative(String name, int value)
	{
		if(value < 0) {
			throw new IndexOutOfBoundsException(name + " is less than zero: " + value);
		}
	}


	private void requireLessThanOrEqualTo(String lhs, int lhsValue, String rhs, int rhsValue)
	{
		if(lhsValue > rhsValue) {
			throw new IndexOutOfBoundsException(lhs + " > " + rhs + ": " + lhsValue + " > " + rhsValue);
		}
	}


	public int compareTo(CharSequence that)
	{
		int max = Math.min(this.length, that.length());
		int i = 0;
		while(i < max) {
			char c1 = sequence[offset + i];
			char c2 = that.charAt(i);
			if(c1 != c2) {
				return c1 - c2;
			}
			i++;
		}
		return this.length - that.length();
	}


	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(length);
		for(int i = offset; i < offset + length; i++) {
			out.writeChar(sequence[i]);
		}
	}


	@Override
	public void readExternal(ObjectInput in) throws IOException
	{
		length = in.readInt();
		sequence = new char[length];
		for(int i = 0; i < length; i++) {
			sequence[i] = in.readChar();
		}
	}


	public char[] toArray()
	{
		char[] dest = new char[length];
		System.arraycopy(sequence, offset, dest, 0, length);
		return dest;
	}
}
