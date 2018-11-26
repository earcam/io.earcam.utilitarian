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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.time.ZoneId.systemDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Test directory structure (where x,y,z are files):
 * 
 * @formatter:off
 * 
 * <pre>
 * └── a
 *     ├── b
 *     │   └── c
 *     │       ├── d
 *     │       │   └── y
 *     │       └── e
 *     │           └── f
 *     │               └── g
 *     │                   └── z
 *     └── x
 * </pre>
 * 
 * @formatter:on
 */
public class RecursiveFilesTest {

	private final Path origin = Paths.get(".", "src", "test", "resources", "a");


	@Test
	public void copy() throws IOException
	{
		Path source = origin;
		Path sink = Paths.get(".", "target", stamp("copy"));
		RecursiveFiles.copy(source, sink);

		assertSameFiles(source, sink);
	}


	private String stamp(String prefix)
	{
		return prefix + "_" + LocalDateTime.now(systemDefault()).toString() + "_" + UUID.randomUUID();
	}


	private void assertSameFiles(Path source, Path sink) throws IOException
	{
		assertSameContents(source, sink, Paths.get("x"));
		assertSameContents(source, sink, Paths.get("b", "c", "d", "y"));
		assertSameContents(source, sink, Paths.get("b", "c", "e", "f", "g", "z"));
	}


	private void assertSameContents(Path source, Path sink, Path file) throws IOException
	{
		byte[] sourceFile = Files.readAllBytes(source.resolve(file));
		byte[] sinkFile = Files.readAllBytes(sink.resolve(file));

		assertThat(sinkFile, is(equalTo(sourceFile)));
	}


	@Test
	public void move() throws IOException
	{
		String stamp = stamp("move");
		Path source = Paths.get(".", "target", stamp, "source");
		Path sink = Paths.get(".", "target", stamp, "sink");
		RecursiveFiles.copy(origin, source);
		assertSameFiles(origin, source);

		RecursiveFiles.move(source, sink);

		assertSameFiles(origin, sink);

		assertThat(source.toFile(), is(not(anExistingDirectory())));
	}


	@Test
	public void delete() throws IOException
	{
		Path source = origin;
		Path sink = Paths.get(".", "target", stamp("delete"));
		RecursiveFiles.copy(source, sink);
		assertSameFiles(origin, sink);

		RecursiveFiles.delete(sink);

		assertThat(sink.toFile(), is(not(anExistingDirectory())));
	}


	@Test
	public void deleteFailsWhenADirectoryIsReadOnly() throws IOException
	{
		Path source = origin;
		Path sink = Paths.get(".", "target", stamp("delete-fail"));
		RecursiveFiles.copy(source, sink);
		assertSameFiles(origin, sink);

		sink.resolve(Paths.get("b", "c")).toFile().setReadOnly();

		try {
			RecursiveFiles.delete(sink);
			fail();
		} catch(AccessDeniedException e) {
			/* noop */
		} finally {
			sink.resolve(Paths.get("b", "c")).toFile().setWritable(true);
			assertThat(sink.toFile(), is(anExistingDirectory()));
		}

	}


	@Test
	public void deleteStillSucceedsWhenAFileIsReadOnly() throws IOException
	{
		Path source = origin;
		Path sink = Paths.get(".", "target", stamp("delete-ro-file"));
		RecursiveFiles.copy(source, sink);
		assertSameFiles(origin, sink);

		RecursiveFiles.delete(sink);

		assertThat(sink.toFile(), is(not(anExistingDirectory())));
	}


	@Test
	public void followSymbolicLink() throws IOException
	{
		Path source = origin;
		Path base = Paths.get(".", "target", stamp("delete-follow-symlink"));
		Path sink = base.resolve("sink");
		RecursiveFiles.copy(source, sink);
		assertSameFiles(origin, sink);

		Path linkTarget = base.resolve(Paths.get("sank", "linkTarget")).toAbsolutePath();
		Path fileUnderLinkTarget = linkTarget.resolve(Paths.get("sunk", "some.file"));
		fileUnderLinkTarget.getParent().toFile().mkdirs();
		Files.write(fileUnderLinkTarget, "this will be deleted".getBytes(UTF_8));

		Path link = sink.resolve(Paths.get("b", "c", "d", "link"));
		Files.createSymbolicLink(link, linkTarget);

		RecursiveFiles.delete(sink);

		assertThat(link.toFile(), is(not(anExistingFile())));
		assertThat(fileUnderLinkTarget.toFile(), is(not(anExistingFile())));
	}


	@Test
	public void doNotFollowSymbolicLink() throws IOException
	{
		Path source = origin;
		Path base = Paths.get(".", "target", stamp("delete-follow-symlink"));
		Path sink = base.resolve("sink");
		RecursiveFiles.copy(source, sink);
		assertSameFiles(origin, sink);

		Path linkTarget = base.resolve(Paths.get("sank", "linkTarget")).toAbsolutePath();
		Path fileUnderLinkTarget = linkTarget.resolve(Paths.get("sunk", "some.file"));
		fileUnderLinkTarget.getParent().toFile().mkdirs();
		Files.write(fileUnderLinkTarget, "this will be deleted".getBytes(UTF_8));

		Path link = sink.resolve(Paths.get("b", "c", "d", "link"));
		Files.createSymbolicLink(link, linkTarget);

		RecursiveFiles.delete(sink, LinkOption.NOFOLLOW_LINKS);

		assertThat(link.toFile(), is(not(anExistingFile())));
		assertThat(fileUnderLinkTarget.toFile(), is(anExistingFile()));
	}


	@Test
	public void copyWillNotOverwriteImplicitly() throws IOException
	{
		String stamp = stamp("no-overwrite");
		Path source = Paths.get(".", "target", stamp, "source");
		Path sink = Paths.get(".", "target", stamp, "sink");
		RecursiveFiles.copy(origin, source);
		RecursiveFiles.copy(origin, sink);

		try {
			RecursiveFiles.copy(source, sink);
			fail();
		} catch(FileAlreadyExistsException e) {}
	}


	@Test
	public void copyWillNotOverwriteFileWithDirectoryImplicitly() throws IOException
	{
		String stamp = stamp("no-overwrite");
		Path source = Paths.get(".", "target", stamp, "source");
		Path sink = Paths.get(".", "target", stamp, "sink");
		RecursiveFiles.copy(origin, source);
		Files.write(sink, "A file already exists".getBytes(UTF_8));

		try {
			RecursiveFiles.copy(source, sink);
			fail();
		} catch(FileAlreadyExistsException e) {}
	}


	@Test
	public void copyWillOverwriteGivenOption() throws IOException
	{
		String stamp = stamp("overwrite");
		Path source = Paths.get(".", "target", stamp, "source");
		Path sink = Paths.get(".", "target", stamp, "sink");
		RecursiveFiles.copy(origin, source);
		RecursiveFiles.copy(origin, sink);

		RecursiveFiles.copy(source, sink, REPLACE_EXISTING);

		assertSameFiles(source, sink);
	}
}
