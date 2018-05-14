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

import static java.util.stream.Collectors.groupingBy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import io.earcam.utilitarian.file.Digestive;

final class FileMap {

	public static final String SHA1_FAILED_KEY = "";


	private FileMap()
	{}


	public static Map<String, List<File>> sha1FileMap(Path siteBaseDir)
	{
		return fileMap(siteBaseDir, FileMap::sha1Hex);
	}


	public static Map<String, List<File>> fileMap(Path siteBaseDir, Function<File, String> mapper)
	{
		return pathStream(siteBaseDir)
				.map(Path::toFile)
				.filter(File::isFile)
				.collect(groupingBy(mapper));
	}


	public static Stream<Path> files(Path siteBaseDir)
	{
		return pathStream(siteBaseDir)
				.filter(Files::isRegularFile);
	}


	private static Stream<Path> pathStream(Path siteBaseDir)
	{
		try {
			return Files.walk(siteBaseDir);
		} catch(IOException e) {
			throw new IllegalArgumentException(e);
		}
	}


	public static String sha1Hex(File file)
	{
		try {
			return Digestive.sha1Hex(new FileInputStream(file));
		} catch(UncheckedIOException | FileNotFoundException e) { // NOSONAR yeah FNFE, got it, understood
			return SHA1_FAILED_KEY;
		}
	}

}
