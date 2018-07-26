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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

public class FilteratorTest {

	@Test
	public void excludeSingleValue()
	{
		List<Integer> filtered = new ArrayList<>();
		Iterator<Integer> iterator = Arrays.asList(1, 2, 3, 42, 4, 5, 6, 7).iterator();

		Filterator<Integer> filterator = new Filterator<Integer>(iterator, 42);
		filterator.forEachRemaining(filtered::add);

		assertThat(filtered, contains(1, 2, 3, 4, 5, 6, 7));
	}


	@Test
	public void doesNotHaveNextWhenWrappedIteratorIsEmpty()
	{
		Iterator<Integer> iterator = Collections.emptyIterator();

		Filterator<Integer> filterator = new Filterator<Integer>(iterator, 42);

		assertThat(filterator.hasNext(), is(false));
	}


	@Test
	public void doesNotHaveNextWhenWrappedIteratorOnlyContainsExclude()
	{
		Iterator<Integer> iterator = Collections.singleton(42).iterator();

		Filterator<Integer> filterator = new Filterator<Integer>(iterator, 42);

		assertThat(filterator.hasNext(), is(false));
	}


	@Test
	public void callingNextOnEmptyIteratorThrows()
	{
		Iterator<Integer> iterator = Collections.emptyIterator();

		Filterator<Integer> filterator = new Filterator<Integer>(iterator, 42);
		try {
			filterator.next();
			fail();
		} catch(NoSuchElementException e) {}
	}


	@Test
	public void callingNextOnSingleElementIteratorWithoutCheckingHasNext()
	{
		Iterator<Integer> iterator = Collections.singleton(42).iterator();

		Filterator<Integer> filterator = new Filterator<Integer>(iterator, 101);
		assertThat(filterator.next(), is(42));
	}


	@Test
	public void repeatedCallsToHasNextDoesNotMoveIterator()
	{
		Iterator<Integer> iterator = Collections.singleton(42).iterator();

		Filterator<Integer> filterator = new Filterator<Integer>(iterator, 101);

		for(int i = 0; i < 100; i++) {
			assertThat(filterator.hasNext(), is(true));
		}

		assertThat(filterator.next(), is(42));
	}
}
