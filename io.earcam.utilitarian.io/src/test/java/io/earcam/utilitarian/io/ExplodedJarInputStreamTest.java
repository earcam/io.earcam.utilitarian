/*-
 * #%L
 * io.earcam.instrumental.archive
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

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.junit.Ignore;
import org.junit.Test;

import io.earcam.unexceptional.Exceptional;

public class ExplodedJarInputStreamTest {

	private static final Path TEST_DIR = Paths.get(".", "target", "test", ExplodedJarInputStreamTest.class.getSimpleName(), UUID.randomUUID().toString());


	// FIXME clean this up, it's hideous
	@Test
	public void disgustingTest() throws Exception
	{
		Path jarDir = TEST_DIR.resolve(Paths.get("explodesToFilesystem"));

		final Class<?> archivedType = ExplodedJarInputStreamTest.class;

		String archivedFile = archivedType.getCanonicalName().replace('.', '/') + ".class";
		Path archivedPath = jarDir.resolve(archivedFile);
		archivedPath.getParent().toFile().mkdirs();
		try(InputStream in = archivedType.getClassLoader().getResourceAsStream(archivedFile)) {
			try(FileOutputStream out = new FileOutputStream(archivedPath.toFile())) {
				IoStreams.transfer(in, out);
			}
		}

		Path mf = jarDir.resolve(Paths.get("META-INF", "MANIFEST.MF"));
		mf.getParent().toFile().mkdirs();

		Manifest archivedManifest = new Manifest();
		archivedManifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		archivedManifest.getMainAttributes().putValue("SomeKey", "SomeValue");
		archivedManifest.write(new FileOutputStream(mf.toFile()));

		boolean manifestChecked = false;
		boolean archivedClassChecked = false;

		try(JarInputStream input = ExplodedJarInputStream.jarInputStreamFrom(jarDir)) {

			Manifest manifest = input.getManifest();
			assertThat(manifest.getMainAttributes().getValue("SomeKey"), is(equalTo("SomeValue")));

			JarEntry entry;
			while((entry = input.getNextJarEntry()) != null) {

				if(entry.isDirectory()) {
					assertThat(entry.getName(), anyOf(
							startsWith("io"),
							is(equalTo("META-INF"))));
				} else {

					if("META-INF/MANIFEST.MF".equals(entry.getName())) {
						manifest = new Manifest();
						manifest.read(input);

						assertThat(manifest.getMainAttributes().getValue("SomeKey"), is(equalTo("SomeValue")));
						manifestChecked = true;
					} else if(!entry.getName().contains("$")) {

						byte[] bytecode = IoStreams.readAllBytes(input);

						assertThat(entry.getSize(), is((long) bytecode.length));
						assertThat(entry.getSize(), is(archivedPath.toFile().length()));

						assertThat(entry.getTime(),
								is(equalTo(Exceptional.apply(Files::readAttributes, archivedPath, BasicFileAttributes.class).creationTime().toMillis())));
						assertThat(entry.getCreationTime(), is(equalTo(Files.getLastModifiedTime(archivedPath, NOFOLLOW_LINKS))));

						new ClassLoader(null) {
							{
								Class<?> type = defineClass(archivedType.getCanonicalName(), bytecode, 0, bytecode.length);

								assertThat(type.getCanonicalName(), is(equalTo(archivedType.getCanonicalName())));

								assertThat(type, is(not(equalTo(archivedType))));
							}
						};
						archivedClassChecked = true;
					}
				}
			}
			assertThat(manifestChecked, is(true));
			assertThat(archivedClassChecked, is(true));
		}
	}


	@Test
	public void explodedJarThrowsWhenPathIsNotADirectory()
	{
		try {
			ExplodedJarInputStream.explodedJar(Paths.get(".", "pom.xml"));
			fail();
		} catch(IOException e) {

		}
	}


	@Ignore // TODO ExplodedJarInputStream doesn't behave like a proper stream... yet
	@Test
	public void asNormalInputStream() throws Exception
	{
		Path outputDir = Paths.get("target", "classes");
		JarInputStream explodedJar = ExplodedJarInputStream.explodedJar(outputDir);

		byte[] bytes = IoStreams.readAllBytes(explodedJar);

		Files.write(Paths.get("target", "oh_noes.jar"), bytes, TRUNCATE_EXISTING, WRITE, CREATE);
	}
}
