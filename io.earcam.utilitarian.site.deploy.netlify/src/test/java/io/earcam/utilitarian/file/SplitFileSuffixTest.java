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

import static io.earcam.utilitarian.file.SplitFileSuffix.splitFileSuffix;
import static java.util.Collections.singleton;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliterator;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class SplitFileSuffixTest {

	@Test
	public void zeroTotalIsFineButUseless()
	{
		Iterator<String> unused = splitFileSuffix(0);
		assertThat(unused.hasNext(), is(false));
	}


	@Test
	public void negativeTotalIsInvalid()
	{
		try {
			splitFileSuffix(-1);
			fail();
		} catch(IllegalArgumentException e) {}
	}


	@Test
	public void whenNegativeIndexThenThrowsIndexOutOfBounds()
	{
		SplitFileSuffix suffices = splitFileSuffix(10);
		try {
			suffices.apply(-1);
			fail();
		} catch(IndexOutOfBoundsException e) {}
	}


	@Test
	public void totalOfOneIsASpecialCase()
	{
		Iterator<String> iterator = splitFileSuffix(singleton("blah"));

		assertThat(iterator.next(), is(equalTo("")));
	}


	@Test
	public void oneInManyIsNotASpecialCase()
	{
		List<String> many = generate(() -> "blah").limit(1001L).collect(toList());
		Iterator<String> iterator = splitFileSuffix(many);

		assertThat(iterator.next(), is(equalTo("0001")));
	}


	@Test
	public void givenNothingAvailableWhenNextIsInvokedThenThrowsNoSuch()
	{
		Iterator<String> noneLeft = splitFileSuffix(0);
		try {
			noneLeft.next();
			fail();
		} catch(NoSuchElementException e) {}

	}


	@Test
	public void nine()
	{
		String splitIndex = splitFileSuffix(9).apply(7);

		assertThat(splitIndex, is(equalTo("8")));
	}


	@Test
	public void ninetyThree()
	{
		String splitIndex = splitFileSuffix(93).apply(5);

		assertThat(splitIndex, is(equalTo("06")));
	}


	@Test
	public void ninetyNine()
	{
		String splitIndex = splitFileSuffix(99).apply(98);

		assertThat(splitIndex, is(equalTo("99")));
	}


	@Test
	public void oneHundred()
	{
		String splitIndex = splitFileSuffix(100).apply(41);

		assertThat(splitIndex, is(equalTo("042")));
	}


	@Test
	public void oneThousandTwoHundredAndThirtyFour()
	{
		String splitIndex = splitFileSuffix(1234).apply(999);

		assertThat(splitIndex, is(equalTo("1000")));
	}


	@Test
	public void oneMillion()
	{
		String splitIndex = splitFileSuffix(1000000).apply(999);

		assertThat(splitIndex, is(equalTo("0001000")));
	}


	@Test
	public void indexOutOfBounds()
	{
		try {
			splitFileSuffix(1234).apply(1234);
			fail();
		} catch(IndexOutOfBoundsException e) {}
	}


	@Test
	public void iterable()
	{
		List<String> suffices = stream(spliterator(splitFileSuffix(10), 10L, IMMUTABLE | NONNULL | ORDERED), false).collect(toList());

		assertThat(suffices, contains("01", "02", "03", "04", "05", "06", "07", "08", "09", "10"));
	}
}
