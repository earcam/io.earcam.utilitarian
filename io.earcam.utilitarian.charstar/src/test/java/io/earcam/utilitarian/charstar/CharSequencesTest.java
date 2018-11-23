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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;

import java.util.Comparator;

import org.junit.jupiter.api.Test;

public class CharSequencesTest {

	@Test
	public void startsWith()
	{
		assertThat(CharSequences.startsWith("startsWith", "starts"), is(true));
	}


	@Test
	public void startsWithIdentical()
	{
		assertThat(CharSequences.startsWith("identical", "identical"), is(true));
	}


	@Test
	public void doesNotStartWith()
	{
		assertThat(CharSequences.startsWith("doesNotStartWith", "startWith"), is(false));
	}


	@Test
	public void doesNotStartWit_()
	{
		assertThat(CharSequences.startsWith("doesNotStartWith", "doesNotStartWit_"), is(false));
	}


	@Test
	public void doesNotStartWithDifferentLength()
	{
		assertThat(CharSequences.startsWith("startWith", "doesNotStartWith"), is(false));
	}


	@Test
	public void endsWith()
	{
		assertThat(CharSequences.endsWith("endsWith", "With"), is(true));
	}


	@Test
	public void endsWithIdentical()
	{
		assertThat(CharSequences.endsWith("identical", "identical"), is(true));
	}


	@Test
	public void doesNotEndWith()
	{
		assertThat(CharSequences.endsWith("doesNotEndWith", "startWith"), is(false));
	}


	@Test
	public void doesNotEndWit_()
	{
		assertThat(CharSequences.endsWith("doesNotEndWith", "EndWit_"), is(false));
	}


	@Test
	public void _oesNotEndWith()
	{
		assertThat(CharSequences.endsWith("doesNotEndWith", "_oesNotEndWith"), is(false));
	}


	@Test
	public void doesNotEndWithDifferentLength()
	{
		assertThat(CharSequences.endsWith("endsWith", "doesNotEndWith"), is(false));
	}


	@Test
	public void same()
	{
		assertThat(CharSequences.same("identical", "identical"), is(true));
	}


	@Test
	public void sameBothNull()
	{
		assertThat(CharSequences.same(null, null), is(true));
	}


	@Test
	public void notSame()
	{
		assertThat(CharSequences.same("identity", "imposter"), is(false));
	}


	@Test
	public void notSameDifferentLength()
	{
		assertThat(CharSequences.same("identical", "identica"), is(false));
	}


	@Test
	public void notSameSecondIsNull()
	{
		assertThat(CharSequences.same("not null", null), is(false));
	}


	@Test
	public void notSameFirstIsNull()
	{
		assertThat(CharSequences.same(null, "not null"), is(false));
	}


	@Test
	public void trimLeading()
	{
		assertThat(CharSequences.trim("\t\t\ttext"), is(equalTo("text")));
	}


	@Test
	public void trimTrailing()
	{
		assertThat(CharSequences.trim("text \r\n\t  "), is(equalTo("text")));
	}


	@Test
	public void trimBothEnds()
	{
		assertThat(CharSequences.trim("\n text \n\t  "), is(equalTo("text")));
	}


	@Test
	public void nothingToTrim()
	{
		assertThat(CharSequences.trim("A short sentence."), is(equalTo("A short sentence.")));
	}


	@Test
	public void replaceIsNullSafe()
	{
		assertThat(CharSequences.replace(null, 'a', 'b'), is(nullValue()));
	}


	@Test
	public void replace()
	{
		CharSequence chars = CharSequences.replace("baa baa black sheep", 'a', 'b');
		assertThat(chars, hasToString(equalTo("bbb bbb blbck sheep")));
	}


	@Test
	public void nullIsEmpty()
	{
		assertThat(CharSequences.isEmpty(null), is(true));
	}


	@Test
	public void zeroLengthStringIsEmpty()
	{
		assertThat(CharSequences.isEmpty(""), is(true));
	}


	@Test
	public void nonZeroLengthStringBuilderIsNotEmpty()
	{
		assertThat(CharSequences.isEmpty(new StringBuilder("not empty")), is(false));
	}


	@Test
	public void indexOfNotFound()
	{
		assertThat(CharSequences.indexOf(new StringBuilder("trust in dexterity"), 'Z'), is(-1));
	}


	@Test
	public void indexOfFirst()
	{
		assertThat(CharSequences.indexOf(new StringBuilder("trust in dexterity"), 't'), is(0));
	}


	@Test
	public void indexOfLast()
	{
		assertThat(CharSequences.indexOf(new StringBuilder("trust in dexterity"), 'y'), is(17));
	}


	@Test
	public void indexOf()
	{
		assertThat(CharSequences.indexOf(new StringBuilder("trust in dexterity"), 'e'), is(10));
	}


	@Test
	public void indexOfStartingFromNonZero()
	{
		assertThat(CharSequences.indexOf(new StringBuilder("trust in dexterity"), 'e', 11), is(13));
	}


	@Test
	public void lastIndexOfNotFound()
	{
		assertThat(CharSequences.lastIndexOf(new StringBuilder("trust in dexterity"), 'Z'), is(-1));
	}


	@Test
	public void lastIndexOf()
	{
		assertThat(CharSequences.lastIndexOf(new StringBuilder("trust in dexterity"), 'e'), is(13));
	}


	@Test
	public void lastIndexOfFirst()
	{
		assertThat(CharSequences.lastIndexOf(new StringBuilder("we trust in dexterity"), 'w'), is(0));
	}


	@Test
	public void lastIndexOfLast()
	{
		assertThat(CharSequences.lastIndexOf(new StringBuilder("trust in dexterity"), 'y'), is(17));
	}


	@Test
	public void toBytes()
	{
		assertThat(CharSequences.toBytes(new StringBuilder("walking on egg shells")), is(equalTo("walking on egg shells".getBytes(UTF_8))));
	}


	@Test
	public void consistentHashCodeBetweenStringBuilderAndString()
	{
		StringBuilder builder = new StringBuilder("a").append(" litt").append("le ").append("bi").append("t more");
		String string = "a little bit more";

		assertThat(CharSequences.hashCode(string), is(equalTo(CharSequences.hashCode(builder))));
	}


	@Test
	public void compareToEqual()
	{
		assertThat(CharSequences.compare("abc", "abc"), is(0));
	}


	@Test
	public void compareToLessThan()
	{
		assertThat(CharSequences.compare("abc", "abd"), is(lessThan(0)));
	}


	@Test
	public void compareToGreaterThan()
	{
		assertThat(CharSequences.compare("abc", "abb"), is(greaterThan(0)));
	}


	@Test
	public void comparator()
	{
		Comparator<CharSequence> comparator = CharSequences.comparator();
		assertThat(comparator.compare("abc", "abb"), is(greaterThan(0)));
	}
}
