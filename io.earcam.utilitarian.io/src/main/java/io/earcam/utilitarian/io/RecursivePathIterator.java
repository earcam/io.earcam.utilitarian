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

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Function;

import io.earcam.unexceptional.Exceptional;

class RecursivePathIterator implements Iterator<Path> {

	private static final Function<Path, DirectoryStream<Path>> DEFAULT_STREAMER = Exceptional.uncheckFunction(Files::newDirectoryStream);
	private final Deque<Iterator<Path>> pending = new ArrayDeque<>();
	private final Function<Path, DirectoryStream<Path>> streamer;
	private Iterator<Path> current;


	RecursivePathIterator(Path root)
	{
		this(root, DEFAULT_STREAMER);
	}


	RecursivePathIterator(Path root, Function<Path, DirectoryStream<Path>> directoryStreamer)
	{
		this(directoryStreamer.apply(root).iterator(), directoryStreamer);
	}


	RecursivePathIterator(Iterator<Path> iterator, Function<Path, DirectoryStream<Path>> directoryStreamer)
	{
		this.current = iterator;
		this.streamer = directoryStreamer;
	}


	@Override
	public boolean hasNext()
	{
		return current.hasNext()
				|| popPending();
	}


	private boolean popPending()
	{
		if(pending.isEmpty()) {
			return false;
		}
		current = pending.pop();
		return hasNext();
	}


	@Override
	public Path next()
	{
		Path next = current.next();
		if(next.toFile().isDirectory()) {
			pending.push(current);
			current = streamer.apply(next).iterator();
		}
		return next;
	}

}
