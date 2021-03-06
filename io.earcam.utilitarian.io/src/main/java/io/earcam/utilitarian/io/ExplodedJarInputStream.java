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

import static io.earcam.unexceptional.Closing.closeAfterAccepting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.annotation.concurrent.NotThreadSafe;

import io.earcam.unexceptional.Exceptional;

/**
 * BEWARE OF LIMITATIONS; CANNOT BE WRAPPED BY ANOTHER JarInputStream, CANNOT BE READ AS A NORMAL INPUT STREAM
 * 
 * TODO performance: {@link ExplodedJarEntry#loadContents()} should just be wrapping FileInputStream...
 */
@SuppressWarnings("squid:MaximumInheritanceDepth") // SonarQube; Not much can be done about this... dirty hack anyhoo
@NotThreadSafe
public final class ExplodedJarInputStream extends JarInputStream {

	private static final Path MANIFEST_PATH = Paths.get("META-INF", "MANIFEST.MF");

	private static class EmptyInputStream extends InputStream {

		public static final InputStream EMPTY_INPUTSTREAM = new EmptyInputStream();


		@Override
		public int read()
		{
			return -1;
		}
	}

	public class ExplodedJarEntry extends JarEntry {

		private Path path;
		private byte[] contents;
		private int position = 0;


		public ExplodedJarEntry(Path path)
		{
			super(directory.relativize(path).toString());
			this.path = path;
			setMethod(STORED);
		}


		public Path path()
		{
			return path;
		}


		@Override
		public boolean isDirectory()
		{
			return path().toFile().isDirectory();
		}


		@Override
		public FileTime getCreationTime()
		{
			return Exceptional.apply(Files::readAttributes, path(), BasicFileAttributes.class).creationTime();
		}


		@Override
		public FileTime getLastModifiedTime()
		{
			return Exceptional.apply(Files::getLastModifiedTime, path());
		}


		@Override
		public long getTime()
		{
			return getLastModifiedTime().toMillis();
		}


		@Override
		public long getSize()
		{
			return Exceptional.apply(Files::size, path());
		}


		private void loadContents()
		{
			if(contents == null) {
				contents = Exceptional.apply(Files::readAllBytes, path());
			}
		}


		/**
		 * @deprecated Never intended for public use.
		 * Aggressively deprecated, class will become final once dropped.
		 * 
		 * @return nothing
		 * @throws UnsupportedOperationException everytime
		 */
		@Deprecated
		public int read()
		{
			throw new UnsupportedOperationException("Never intended for public use. Agressively deprecated.");
		}


		public int read(byte[] b, int off, int len)
		{
			loadContents();
			int remaining = available();
			if(remaining == 0) {
				return -1;
			}
			int length = Math.min(remaining, len);
			System.arraycopy(contents, position, b, off, length);
			position += length;
			return length;
		}


		int available()
		{
			loadContents();
			return contents.length - position;
		}
	}

	private Iterator<Path> iterator;
	private Path directory;
	private ExplodedJarEntry current;


	private ExplodedJarInputStream(Path directory, Iterator<Path> iterator) throws IOException
	{
		super(EmptyInputStream.EMPTY_INPUTSTREAM, false);
		this.directory = directory.toRealPath();
		this.iterator = iterator;
	}


	/**
	 * If the {@code path} parameter is a directory then returns an {@link ExplodedJarInputStream},
	 * otherwise, the {@code path} parameter is a file so, returns a {@link JarInputStream}
	 * 
	 * @param path the location on a filesystem
	 * @return a jar input stream
	 * @throws IOException
	 * 
	 * @see {@link #explodedJar(Path)}
	 */
	public static JarInputStream jarInputStreamFrom(Path path) throws IOException
	{
		return jarInputStreamFrom(path.toFile());
	}


	/**
	 * If the {@code path} parameter is a directory then returns an {@link ExplodedJarInputStream},
	 * otherwise, the {@code path} parameter is a file so, returns a {@link JarInputStream}
	 * 
	 * @param path the location on a filesystem
	 * @return a jar input stream
	 * @throws IOException
	 */
	public static JarInputStream jarInputStreamFrom(File path) throws IOException
	{
		return path.isDirectory() ? explodedJar(path) : new JarInputStream(new FileInputStream(path));
	}


	/**
	 * Treat the <b>directory</b> as an exploded JAR file
	 * 
	 * @param directory
	 * @return a jar input stream, which can be read using {@link JarEntry} methods
	 * @throws IOException
	 */
	public static ExplodedJarInputStream explodedJar(File directory) throws IOException
	{
		return explodedJar(directory.toPath());
	}


	/**
	 * Treat the <b>directory</b> as an exploded JAR file
	 * 
	 * @param directory
	 * @return a jar input stream, which can be read using {@link JarEntry} methods
	 * @throws IOException
	 */
	public static ExplodedJarInputStream explodedJar(Path directory) throws IOException
	{
		if(!directory.toFile().isDirectory()) {
			throw new IOException("'" + directory + "' is not a directory");
		}
		RecursivePathIterator rpi = new RecursivePathIterator(directory);
		return new ExplodedJarInputStream(directory, new Filterator<Path>(rpi, MANIFEST_PATH));
	}


	@Override
	public JarEntry getNextJarEntry() throws IOException
	{
		current = iterator.hasNext() ? new ExplodedJarEntry(iterator.next().toRealPath()) : null;
		return current;
	}


	@Override
	public Manifest getManifest()
	{
		Manifest manifest = null;
		Path file = directory.resolve(MANIFEST_PATH);
		if(file.toFile().exists()) {
			manifest = new Manifest();
			closeAfterAccepting(FileInputStream::new, file.toFile(), manifest::read);
		}
		return manifest;
	}


	private void checkCurrent()
	{
		if(current == null) {
			throw new UnsupportedOperationException(ExplodedJarInputStream.class + " does not work as a regular InputStream");
		}
	}


	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		checkCurrent();
		return current.read(b, off, len);
	}


	@Override
	public int available() throws IOException
	{
		checkCurrent();
		return current.available();
	}
}
