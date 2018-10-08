/*-
 * #%L
 * io.earcam.instrumental.io.file
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
package io.earcam.utilitarian.io.file;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * Wise to invoke these methods with {@link LinkOption#NOFOLLOW_LINKS}
 */
public final class RecursiveFiles {

	private abstract static class AbstractVisitor extends SimpleFileVisitor<Path> {

		final Path sink;
		final Path source;
		final CopyOption[] options;

		Path sinkSub;


		public AbstractVisitor(Path source, Path sink, CopyOption... options)
		{
			this.source = source;
			this.sink = sink;
			this.options = options;
		}


		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
		{
			sinkSub = sink.resolve(source.relativize(dir));
			sinkSub.toFile().mkdirs();
			return CONTINUE;
		}


		@Override
		public FileVisitResult postVisitDirectory(Path directory, IOException thrown) throws IOException
		{
			super.postVisitDirectory(directory, thrown);
			return postVisitDirectory(directory);
		}


		protected abstract FileVisitResult postVisitDirectory(Path directory) throws IOException;


		@Override
		public abstract FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException;
	}

	private static final class DeleteVisitor extends AbstractVisitor {

		public DeleteVisitor(Path source, CopyOption... options)
		{
			super(source, source, options);
		}


		@Override
		public FileVisitResult postVisitDirectory(Path directory) throws IOException
		{
			Files.delete(directory);
			return CONTINUE;
		}


		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			Files.delete(file);
			return CONTINUE;
		}
	}

	private static final class MoveVisitor extends AbstractVisitor {

		public MoveVisitor(Path source, Path sink, CopyOption... options)
		{
			super(source, sink, options);
		}


		@Override
		protected FileVisitResult postVisitDirectory(Path directory) throws IOException
		{
			Files.delete(directory);
			return CONTINUE;
		}


		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			Files.move(file, sinkSub.resolve(file.getFileName()), options);
			return CONTINUE;
		}
	}

	private static final class CopyVisitor extends AbstractVisitor {

		public CopyVisitor(Path source, Path sink, CopyOption... options)
		{
			super(source, sink, options);
		}


		@Override
		protected FileVisitResult postVisitDirectory(Path directory)
		{
			return CONTINUE;
		}


		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			Files.copy(file, sinkSub.resolve(file.getFileName()), options);
			return CONTINUE;
		}
	}


	private RecursiveFiles()
	{}


	/**
	 * Wise to invoke this with {@link LinkOption#NOFOLLOW_LINKS}
	 * 
	 * @param path
	 * @param options
	 * @throws IOException
	 */
	public static void delete(Path path, LinkOption... options) throws IOException
	{
		recurse(path, new DeleteVisitor(path, options));
	}


	/**
	 * Wise to invoke this with {@link LinkOption#NOFOLLOW_LINKS}
	 * 
	 * @param source
	 * @param sink
	 * @param options
	 * @throws IOException
	 */
	public static void move(Path source, Path sink, CopyOption... options) throws IOException
	{
		recurse(source, new MoveVisitor(source, sink, options));
	}


	private static void recurse(Path source, AbstractVisitor visitor) throws IOException
	{
		boolean noFollow = Arrays.asList(visitor.options).contains(NOFOLLOW_LINKS);
		EnumSet<FileVisitOption> options = noFollow ? EnumSet.noneOf(FileVisitOption.class) : EnumSet.of(FOLLOW_LINKS);

		Files.walkFileTree(source, options, Integer.MAX_VALUE, visitor);
	}


	/**
	 * Wise to invoke this with {@link LinkOption#NOFOLLOW_LINKS}
	 * 
	 * @param source
	 * @param sink
	 * @param options
	 * @throws IOException
	 */
	public static void copy(Path source, Path sink, CopyOption... options) throws IOException
	{
		recurse(source, new CopyVisitor(source, sink, options));
	}
}
