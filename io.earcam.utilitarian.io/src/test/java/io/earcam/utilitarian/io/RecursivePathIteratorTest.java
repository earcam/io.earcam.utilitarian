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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class RecursivePathIteratorTest {

	private static final Path TEST_DIR = Paths.get(".", "target", "test", RecursivePathIteratorTest.class.getSimpleName(), UUID.randomUUID().toString());


	/**
	 * @formatter:off
	 * 
	 * ├── a00
	 * │   └── a10
	 * │       ├── a20
	 * │       │   └── a30
	 * │       │       └── a40 (f)
	 * │       └── a21 (f)
	 * ├── b00
	 * │   ├── b10
	 * │   └── b20 (f)
	 * └── c00 (f)
	 * 
	 * 
	 * @formatter:on
	 * 
	 * @throws IOException
	 */
	@Test
	public void deeper() throws IOException
	{
		Path baseDir = TEST_DIR.resolve("deeper");

		Path a00 = baseDir.resolve(Paths.get("a00"));
		Path a10 = a00.resolve("a10");
		Path a20 = a10.resolve("a20");
		Path a30 = a20.resolve("a30");

		a30.toFile().mkdirs();

		Path b00 = baseDir.resolve("b00");
		Path b10 = b00.resolve("b10");
		b10.toFile().mkdirs();

		Files.write(a30.resolve("a40"), "this is a40".getBytes(UTF_8));
		Files.write(a10.resolve("a21"), "this is a21".getBytes(UTF_8));
		Files.write(b00.resolve("b20"), "this is b20".getBytes(UTF_8));
		Files.write(baseDir.resolve("c00"), "this is c00".getBytes(UTF_8));

		RecursivePathIterator iterator = new RecursivePathIterator(baseDir);

		List<Path> paths = new ArrayList<>();
		iterator.forEachRemaining(p -> paths.add(baseDir.relativize(p)));

		assertThat(paths, containsInAnyOrder(

				Paths.get("a00"),
				Paths.get("a00", "a10"),
				Paths.get("a00", "a10", "a20"),
				Paths.get("a00", "a10", "a20", "a30"),
				Paths.get("a00", "a10", "a20", "a30", "a40"),
				Paths.get("a00", "a10", "a21"),
				Paths.get("b00"),
				Paths.get("b00", "b10"),
				Paths.get("b00", "b20"),
				Paths.get("c00")));
	}
}
