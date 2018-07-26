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

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

class Filterator<E> implements Iterator<E> {

	private Iterator<E> iterator;
	private Set<E> excludes;
	private E next;
	private boolean checked;


	Filterator(Iterator<E> iterator, E exclude)
	{
		this(iterator, Collections.singleton(exclude));
	}


	Filterator(Iterator<E> iterator, Set<E> excludes)
	{
		this.iterator = iterator;
		this.excludes = excludes;
	}


	@Override
	public boolean hasNext()
	{
		return checked || (iterator.hasNext() && checkNext());
	}


	private boolean checkNext()
	{
		next = iterator.next();
		if(excludes.contains(next)) {
			return hasNext();
		}
		checked = true;
		return true;
	}


	@Override
	public E next()
	{
		if(!(checked || hasNext())) {
			throw new NoSuchElementException();
		}
		checked = false;
		return next;
	}
}
