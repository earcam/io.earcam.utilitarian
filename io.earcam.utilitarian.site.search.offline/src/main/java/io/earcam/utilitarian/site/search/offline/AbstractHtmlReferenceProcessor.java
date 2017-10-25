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

import java.io.File;
import java.nio.file.Path;
import java.util.function.Predicate;

abstract class AbstractHtmlReferenceProcessor extends AbstractHtmlProcessor {

	private final Path main;
	private final Path test;
	private final String titlePrefix;


	AbstractHtmlReferenceProcessor(Path main, Path test, String titlePrefix)
	{
		this.main = main;
		this.test = test;
		this.titlePrefix = titlePrefix;
	}


	@Override
	public void process(Document document)
	{
		if(isHtml(document) && !document.hasRaw()) {
			Path root = refDocsRoot(document);
			if(root != null) {
				if(skip(document)) {
					nullContent(document);
				} else {
					proceed(document, root);
				}
			}
		}
	}


	private void nullContent(Document document)
	{
		document.field(Document.RAW_TEXT, "");
	}


	private void proceed(Document document, Path root)
	{
		String fqn = root.relativize(document.file()).toString();
		fqn = fqn.substring(0, fqn.lastIndexOf('.')).replace(File.separatorChar, '.');

		String simpleName = document.file().getFileName().toString();
		simpleName = simpleName.substring(0, simpleName.lastIndexOf('.'));

		document.field(Document.RAW_TEXT, fqn + " " + simpleName);
		document.field(Document.TITLE, titlePrefix + " " + simpleName);
		document.tokens().add(fqn);
		document.tokens().add(simpleName);
	}


	protected abstract boolean skip(Document document);


	private Path refDocsRoot(Document document)
	{
		return matchingParent(document, p -> p.endsWith(main) || p.endsWith(test));
	}


	protected final Path matchingParent(Document document, Predicate<Path> match)
	{
		Path path = document.file();
		do {
			if(match.test(path)) {
				return path;
			}
		} while((path = path.getParent()) != null);
		return null;
	}


	protected final boolean hasMatchingParent(Document document, Predicate<Path> match)
	{
		return matchingParent(document, match) != null;
	}
}
