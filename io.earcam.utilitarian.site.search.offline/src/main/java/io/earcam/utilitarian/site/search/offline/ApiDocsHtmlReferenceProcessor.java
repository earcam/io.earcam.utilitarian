/*-
 * #%L
 * io.earcam.utilitarian.site.search.offline
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
package io.earcam.utilitarian.site.search.offline;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ApiDocsHtmlReferenceProcessor extends AbstractHtmlReferenceProcessor {

	private static final Path APIDOCS = Paths.get("apidocs");
	private static final Path TESTAPIDOCS = Paths.get("testapidocs");

	private static final Set<Path> SKIPPED_FILENAMES = new HashSet<>(Arrays.asList(
			Paths.get("allclasses-frame.html"),
			Paths.get("allclasses-noframe.html"),
			Paths.get("constant-values.html"),
			Paths.get("deprecated-list.html"),
			Paths.get("help-doc.html"),
			Paths.get("index-all.html"),
			Paths.get("index.html"),
			Paths.get("overview-frame.html"),
			Paths.get("overview-summary.html"),
			Paths.get("overview-tree.html"),
			Paths.get("package-frame.html"),
			Paths.get("package-summary.html"),
			Paths.get("package-tree.html"),
			Paths.get("package-use.html"),
			Paths.get("serialized-form.html")));


	public ApiDocsHtmlReferenceProcessor()
	{
		super(APIDOCS, TESTAPIDOCS, "Javadoc for");
	}


	@Override
	protected boolean skip(Document document)
	{
		return SKIPPED_FILENAMES.contains(document.file().getFileName())
				|| hasMatchingParent(document, p -> p.endsWith("class-use"));
	}
}
