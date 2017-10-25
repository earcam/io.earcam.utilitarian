/*-
 * #%L
 * io.earcam.utilitarian.site.deploy.netlify
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
package io.earcam.utilitarian.site.deploy.netlify;

import static io.earcam.utilitarian.site.deploy.netlify.FileMap.SHA1_FAILED_KEY;
import static io.earcam.utilitarian.site.deploy.netlify.FileMap.sha1FileMap;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The dataset contains 6 files, with 2 having the same content and therefore same SHA1
 * @author caspar
 *
 */
public class FileMapTest {

	private static final Path SITE_BASE_DIR = Paths.get("src", "test", "resources", "faux-site", "dummy-site");

	private static Map<String, List<File>> fileMap;


	@BeforeClass
	public static void initialize()
	{
		fileMap = sha1FileMap(SITE_BASE_DIR);
	}


	@Test
	public void allFilesIndexed()
	{
		assertThat(fileMap, is(aMapWithSize(6)));
		assertThat(flatValuesOf(fileMap), hasSize(8));
		assertThat(fileMap, not(hasKey(SHA1_FAILED_KEY)));
	}


	private <T> List<T> flatValuesOf(Map<?, List<T>> map)
	{
		return map.values().stream().flatMap(List::stream).collect(toList());
	}


	@Test
	public void duplicatesAreHeldInMultimap()
	{
		assertThat(fileMap.get("45AE980E7FDBADBC29B05950E1E154EE8EF6A5C7"), hasSize(2));
	}


	@Test
	public void emptyFilesAreHeldAsDuplicatesInMultimap()
	{
		assertThat(fileMap.get("DA39A3EE5E6B4B0D3255BFEF95601890AFD80709"), hasSize(2));
	}


	@Test
	public void contentCorrect()
	{
		assertThat(fileMap, hasKey("611945FD2A3962295F044E9C98FE725C21494983"));
		assertThat(fileMap.get("611945FD2A3962295F044E9C98FE725C21494983"), contains(SITE_BASE_DIR.resolve("directory a/directory-aa/index.html").toFile()));
		//HAMCREST WTF?	assertThat(files, hasEntry("611945FD2A3962295F044E9C98FE725C21494983", contains( SITE_BASE_DIR.resolve("directory-a/directory-aa/index.html").toFile() )));
	}


	@Test
	public void baseDirectoryNotFoundThrownAsIllegalArgument()
	{
		Path nonexistentBaseDir = Paths.get("/", "this", "path", "should", "never", "exist", ",", "hopefully", "not", " - ", "but", "anyhoo");
		assumeThat(nonexistentBaseDir.toFile().isDirectory(), is(false));
		try {
			sha1FileMap(nonexistentBaseDir);
			fail();
		} catch(IllegalArgumentException e) {}
	}


	@Test
	public void sha1HexReturnsEmptyStringIfFileNotFound()
	{
		File file = Paths.get("/", "this", "file", "should", "never", "exist", ",", "meh").toFile();

		assertThat(FileMap.sha1Hex(file), is(equalTo(SHA1_FAILED_KEY)));
	}
}
