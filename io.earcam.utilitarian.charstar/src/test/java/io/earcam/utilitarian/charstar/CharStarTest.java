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

import static io.earcam.unexceptional.Closing.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

public class CharStarTest {

	private final CharStar abc = CharSequences.charSequence(new char[] { 'a', 'b', 'c' });


	@Test
	public void equal()
	{
		Object same = CharSequences.charSequence("abc");
		assertThat(abc, is(equalTo(same)));
	}


	@Test
	public void notEqualAtStart()
	{
		Object same = CharSequences.charSequence("Abc");
		assertThat(abc, is(not(equalTo(same))));
	}


	@Test
	public void notEqualInMiddle()
	{
		Object same = CharSequences.charSequence("aBc");
		assertThat(abc, is(not(equalTo(same))));
	}


	@Test
	public void notEqualAtEnd()
	{
		Object same = CharSequences.charSequence("abC");
		assertThat(abc, is(not(equalTo(same))));
	}


	@Test
	public void notEqualAtEndDifferentLength()
	{
		Object same = CharSequences.charSequence("abcd");
		assertThat(abc, is(not(equalTo(same))));
	}


	@Test
	public void notEqualToNullObject()
	{
		assertFalse(abc.equals((Object) null));
	}


	@Test
	public void notEqualToNullInstance()
	{
		assertFalse(abc.equals((CharStar) null));
	}


	@Test
	public void notEqualToEquivalentString()
	{
		assertThat(abc, is(not(equalTo("abc"))));
	}


	@Test
	public void compareToEqual()
	{
		assertThat(abc.compareTo("abc"), is(0));
	}


	@Test
	public void compareToLessThan()
	{
		assertThat(abc.compareTo("acdc"), is(lessThan(0)));
	}


	@Test
	public void compareToGreaterThan()
	{
		assertThat(abc.compareTo("abbbbracadabra"), is(greaterThan(0)));
	}


	@Test
	public void consistentHashCodeWithString()
	{
		String string = "some text";
		CharStar sequence = CharSequences.charSequence(string);

		assertThat(sequence.hashCode(), is(equalTo(string.hashCode())));
	}


	@Test
	public void backedSubsequenceMaintainsMutableState()
	{
		char[] mutable = { 'h', 'o', ' ', 'h', 'u', 'm' };
		CharStar sequence = CharSequences.backedCharSequence(mutable);

		CharStar humAsYay = sequence.subSequence(3, 6);

		char[] expected = new char[3];
		expected[0] = mutable[3] = 'y';
		expected[1] = mutable[4] = 'a';
		expected[2] = mutable[5] = 'y';

		assertThat(humAsYay.toArray(), is(equalTo(expected)));
	}


	@Test
	public void subsequenceInvalidWhenStartIndexIsNegative()
	{
		CharStar sequence = CharSequences.charSequence("this will not end well");
		try {
			sequence.subSequence(-1, 1);
			fail();
		} catch(IndexOutOfBoundsException oob) {}
	}


	@Test
	public void subsequenceInvalidWhenEndIndexIsNegative()
	{
		CharStar sequence = CharSequences.charSequence("this will not end well");
		try {
			sequence.subSequence(1, -1);
			fail();
		} catch(IndexOutOfBoundsException oob) {}
	}


	@Test
	public void subsequenceInvalidWhenStartIndexIsGreaterThanEndIndex()
	{
		CharStar sequence = CharSequences.charSequence("this will not end well");
		try {
			sequence.subSequence(2, 0);
			fail();
		} catch(IndexOutOfBoundsException oob) {}
	}


	@Test
	public void subsequenceToStringIsOnlySubsequence()
	{
		CharStar sequence = CharSequences.charSequence("not the whole thing");

		CharStar subSequence = sequence.subSequence(8, 13);

		assertThat(subSequence, hasToString(equalTo("whole")));
	}


	@Test
	public void serializableSubsequence()
	{
		String hole = "the whole";
		CharStar sequence = CharSequences.charSequence("not " + hole + " thing");

		CharStar rehydrated = deserialize(serialize(sequence.subSequence(4, 13)), CharStar.class);

		assertThat(rehydrated, hasToString(equalTo(hole)));
	}


	public static byte[] serialize(Object object)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		closeAfterAccepting(ObjectOutputStream::new, baos, object, ObjectOutputStream::writeObject);
		return baos.toByteArray();
	}


	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] serialized, Class<T> type)
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
		return (T) closeAfterApplying(ObjectInputStream::new, bais, ObjectInputStream::readObject);
	}

}
