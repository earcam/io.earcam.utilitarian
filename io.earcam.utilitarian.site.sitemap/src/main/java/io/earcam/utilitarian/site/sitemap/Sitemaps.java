/*-
 * #%L
 * io.earcam.utilitarian.site.sitemap
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
package io.earcam.utilitarian.site.sitemap;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.WillNotClose;

import io.earcam.unexceptional.Closing;
import io.earcam.unexceptional.EmeticStream;
import io.earcam.unexceptional.Exceptional;

/**
 * Static entry point for <a href="https://sitemaps.org">Sitemaps</a>
 *
 */
public final class Sitemaps {

	private static final String GENERATED_FILE_SITEMAPS = ".io.earcam.utilitarian.site.sitemap.list";
	private static final String GENERATED_FILE_SITEMAP_PARAMETERS = ".io.earcam.utilitarian.site.sitemap.parameters.ser";
	private static final String GENERATED_FILE_SITEMAP_INDICES = ".io.earcam.utilitarian.site.sitemap.index.list";

	private static final byte[] NL = bytes(System.lineSeparator());


	private Sitemaps()
	{}


	/**
	 * Based on the parameters, generates an arbitrary number of sitemap.xml files
	 *
	 * @param parameters
	 * @return path to a text file cache containing the filenames of generated sitemaps
	 * @throws IOException if unable to write the files or cache
	 */
	public static Path create(SitemapParameters parameters)
	{
		File file = parameters.targetDir.resolve(GENERATED_FILE_SITEMAP_PARAMETERS).toFile();
		Closing.closeAfterAccepting(FileOutputStream::new, file, parameters::serialize);
		Path generated = parameters.targetDir.resolve(GENERATED_FILE_SITEMAPS);
		Closing.closeAfterAccepting(FileOutputStream::new, generated.toFile(), parameters, Sitemaps::doCreate);
		return generated;
	}


	private static void doCreate(FileOutputStream output, SitemapParameters parameters) throws IOException
	{
		Consumer<Path> generatedFileRecorder = generatedFileRecorder(output);
		Sitemap sitemap = new Sitemap(parameters, generatedFileRecorder);
		sitemap.run();
	}


	private static Consumer<Path> generatedFileRecorder(OutputStream output)
	{
		return Exceptional.uncheckConsumer(p -> {
			output.write(bytes(p.toAbsolutePath()));
			output.write(NL);
		});
	}


	private static byte[] bytes(Object instance)
	{
		return instance.toString().getBytes(UTF_8);
	}


	/**
	 * Creates sitemap-index.xml file(s) for all sitemap.xml files listed in any cache
	 * file ( {@value #GENERATED_FILE_SITEMAPS} ) in the {@code targetDirs}
	 *
	 * @param targetDir where to write the index/indices
	 * @param targetDirs where to look for sitemap listing cache files
	 * @return a path to a cache file ({@value #GENERATED_FILE_SITEMAP_INDICES}) listing the created index/indices
	 * @throws IOException if unable to write the files or read the caches
	 */
	public static Path index(Path targetDir, Stream<Path> targetDirs)
	{
		SitemapParameters parameters = new SitemapParameters();
		File file = targetDir.resolve(GENERATED_FILE_SITEMAP_PARAMETERS).toFile();
		Closing.closeAfterAccepting(FileInputStream::new, file, parameters::deserialize);
		return index(parameters, targetDirs);
	}


	public static Path index(SitemapParameters parameters, Stream<Path> targetDirs)
	{
		Path generated = parameters.targetDir.resolve(GENERATED_FILE_SITEMAP_INDICES);
		Closing.closeAfterAccepting(FileOutputStream::new, generated.toFile(), o -> doIndex(parameters, targetDirs, o));
		return generated;
	}


	private static void doIndex(SitemapParameters parameters, Stream<Path> targetDirs, OutputStream output) throws IOException
	{
		Consumer<Path> generatedFileRecorder = generatedFileRecorder(output);

		SitemapIndex index = new SitemapIndex(parameters, generatedFileRecorder);

		Stream<Entry<SitemapParameters, Stream<String>>> map = targetDirs
				.map(d -> new AbstractMap.SimpleEntry<SitemapParameters, Stream<String>>(
						Exceptional.apply(Sitemaps::hydrateSitemapParameters, d),
						Exceptional.apply(Files::lines, d.resolve(GENERATED_FILE_SITEMAPS))));

		index.accept(map);
	}


	private static SitemapParameters hydrateSitemapParameters(Path targetDir) throws IOException
	{
		FileInputStream input = new FileInputStream(targetDir.resolve(GENERATED_FILE_SITEMAP_PARAMETERS).toFile());
		return new SitemapParameters().deserialize(input);
	}


	/**
	 * Submits sitemap indices to a given list of search engines
	 *
	 * @param targetDir
	 * @param baseUrl
	 * @param hosts
	 * @return
	 */
	public static String submit(Path targetDir, URI baseUrl, List<String> hosts)
	{
		return SitemapSubmission.submit(hosts, baseUrl, targetDir, sitemapsIndicesIn(targetDir));
	}


	private static Stream<Path> sitemapsIndicesIn(Path targetDir)
	{
		Path list = targetDir.resolve(GENERATED_FILE_SITEMAP_INDICES);

		return Exceptional.apply(Files::lines, list)
				.map(Paths::get);
	}


	/**
	 * <p>
	 * Generates a {@code robots.txt} file in the {@code targetDir}, based on
	 * the sitemap indices cache list there.
	 * </p>
	 *
	 * <p>
	 * If no sitemap indices exist then a permissive robots.txt will still be generated.
	 * </p>
	 *
	 * @param targetDir the directory to read the cache list from, and write the robots.txt file to
	 * @throws UncheckedIOException if unable to write the file or read the cache
	 */
	public static void robotsTxt(Path targetDir)
	{
		File robotsTxt = targetDir.resolve("robots.txt").toFile();

		Closing.closeAfterAccepting(FileOutputStream::new, robotsTxt, targetDir, Sitemaps::writeRobotsTxt);
	}


	/**
	 * Generates a {@code robots.txt} file, linking to sitemap indices for cache file in {@code targetDir}
	 *
	 * @param output the stream for {@code robots.txt} content
	 * @param targetDir where to load the cache file ({@value #GENERATED_FILE_SITEMAP_INDICES}) from
	 * @throws IOException if unable to write the file or read the cache
	 */
	private static void writeRobotsTxt(@WillNotClose OutputStream output, Path targetDir) throws IOException
	{
		output.write(bytes(
				"User-agent: *\n" +
						"Allow: /\n" +
						"Disallow:\n" +
						"\n"));

		EmeticStream.emesis(sitemapsIndicesIn(targetDir)).forEach(s -> {
			output.write(bytes("sitemap: ./"));
			output.write(bytes(s.getFileName().toString()));
		});

	}
}
