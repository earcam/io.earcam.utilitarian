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

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
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

import org.hamcrest.Matchers;
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

		Path archivedPath = writeArchiveClass(jarDir, archivedType);

		archiveManifest(jarDir);

		boolean manifestChecked = false;
		boolean archivedClassChecked = false;

		try(JarInputStream input = ExplodedJarInputStream.jarInputStreamFrom(jarDir)) {

			Manifest manifest = input.getManifest();
			assertManifest(manifest);

			JarEntry entry;
			while((entry = input.getNextJarEntry()) != null) {

				if(entry.isDirectory()) {
					assertThat(entry.getName(), anyOf(
							startsWith("io"),
							is(equalTo("META-INF"))));
				} else {

					if("META-INF/MANIFEST.MF".equals(entry.getName())) {
						assertArchivedManifest(input);
						manifestChecked = true;
					} else if(!entry.getName().contains("$")) {

						assertArchivedClass(archivedType, archivedPath, input, entry);
						archivedClassChecked = true;
					}
				}
			}
			assertThat(manifestChecked, is(true));
			assertThat(archivedClassChecked, is(true));
		}
	}


	private void assertArchivedManifest(JarInputStream input) throws IOException
	{
		Manifest manifest = new Manifest(input);
		assertManifest(manifest);
	}


	private void assertManifest(Manifest manifest)
	{
		assertThat(manifest.getMainAttributes().getValue("SomeKey"), is(equalTo("SomeValue")));
	}


	private void assertArchivedClass(final Class<?> archivedType, Path archivedPath, JarInputStream input, JarEntry entry) throws IOException
	{
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
	}


	private void archiveManifest(Path jarDir) throws IOException, FileNotFoundException
	{
		Path mf = jarDir.resolve(Paths.get("META-INF", "MANIFEST.MF"));
		mf.getParent().toFile().mkdirs();

		Manifest archivedManifest = new Manifest();
		archivedManifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		archivedManifest.getMainAttributes().putValue("SomeKey", "SomeValue");
		archivedManifest.write(new FileOutputStream(mf.toFile()));
	}


	private Path writeArchiveClass(Path jarDir, final Class<?> archivedType) throws IOException, FileNotFoundException
	{
		String archivedFile = archivedType.getCanonicalName().replace('.', '/') + ".class";
		Path archivedPath = jarDir.resolve(archivedFile);
		archivedPath.getParent().toFile().mkdirs();
		try(InputStream in = archivedType.getClassLoader().getResourceAsStream(archivedFile)) {
			try(FileOutputStream out = new FileOutputStream(archivedPath.toFile())) {
				IoStreams.transfer(in, out);
			}
		}
		return archivedPath;
	}


	@Test
	public void explodedFromFilesystemWithoutManifest() throws Exception
	{
		Path jarDir = TEST_DIR.resolve(Paths.get("explodedFromFilesystemWithoutManifest"));

		final Class<?> archivedType = ExplodedJarInputStreamTest.class;

		Path archivedPath = writeArchiveClass(jarDir, archivedType);

		boolean manifestChecked = false;
		boolean archivedClassChecked = false;

		try(JarInputStream input = ExplodedJarInputStream.jarInputStreamFrom(jarDir)) {

			Manifest manifest = input.getManifest();
			assertThat(manifest.getMainAttributes().keySet(), is(empty()));

			JarEntry entry;
			while((entry = input.getNextJarEntry()) != null) {

				if(entry.isDirectory()) {
					assertThat(entry.getName(), anyOf(
							startsWith("io"),
							is(equalTo("META-INF"))));
				} else {

					if("META-INF/MANIFEST.MF".equals(entry.getName())) {
						manifestChecked = true;
					} else if(!entry.getName().contains("$")) {

						assertArchivedClass(archivedType, archivedPath, input, entry);
						archivedClassChecked = true;
					}
				}
			}
			assertThat(manifestChecked, is(false));
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


	@Test
	public void jarInputStreamFromJarFile() throws IOException
	{
		String resource = Matchers.class.getClassLoader().getResource(Matchers.class.getCanonicalName().replace('.', '/') + ".class").toString();
		resource = resource.replaceFirst("jar:file:", "").replaceAll("!.*", "");
		Path jarFile = Paths.get(resource);

		try(JarInputStream input = ExplodedJarInputStream.jarInputStreamFrom(jarFile)) {

			Manifest manifest = input.getManifest();
			assertThat(manifest.getMainAttributes().getValue("Implementation-Vendor"), is(equalTo("hamcrest.org")));
		}
	}


	@Test // TODO ExplodedJarInputStream doesn't behave like a proper stream... yet
	public void failsAsNormalInputStream() throws IOException
	{
		Path outputDir = Paths.get("target", "test-classes");
		JarInputStream explodedJar = ExplodedJarInputStream.explodedJar(outputDir);

		try {
			IoStreams.readAllBytes(explodedJar);
			fail();
		} catch(UnsupportedOperationException uoe) {}
	}


	@Test // TODO ExplodedJarInputStream doesn't behave like a proper stream... yet
	public void failsAsNormalInputStreamWithNothingAvailable() throws IOException
	{
		Path outputDir = Paths.get("target", "test-classes");
		JarInputStream explodedJar = ExplodedJarInputStream.explodedJar(outputDir);

		try {
			explodedJar.available();
			fail();
		} catch(UnsupportedOperationException uoe) {}
	}


	@Test // TODO ExplodedJarInputStream doesn't behave like a proper stream... yet
	public void availableCanBeInvokedOnTheEntries() throws IOException
	{
		Path outputDir = Paths.get("target", "test-classes");
		JarInputStream explodedJar = ExplodedJarInputStream.explodedJar(outputDir);

		JarEntry nextJarEntry;
		do {
			nextJarEntry = explodedJar.getNextJarEntry();
		} while(nextJarEntry.isDirectory());

		assertThat(explodedJar.available(), is(greaterThanOrEqualTo(0)));
	}
}
