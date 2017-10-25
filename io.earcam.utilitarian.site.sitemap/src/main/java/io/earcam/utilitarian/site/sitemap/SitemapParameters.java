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

import static io.earcam.unexceptional.Exceptional.uri;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillClose;

import io.earcam.unexceptional.Closing;
import io.earcam.unexceptional.Exceptional;

//False positive, non-serializable fields on an Externalizable do not need to be made transient
@SuppressWarnings("squid:S1948")
@ParametersAreNonnullByDefault
public class SitemapParameters implements Externalizable {

	private static final long serialVersionUID = 337311296864382134L;

	public static final Pattern INCLUDE_ALL = Pattern.compile("^.*$");

	public static class SitemapOptions implements Externalizable {

		private static final long serialVersionUID = 8178383333097167552L;

		private boolean gzip;
		private Pattern include = INCLUDE_ALL;


		@Override
		public void writeExternal(ObjectOutput out) throws IOException
		{
			out.writeBoolean(gzip);
			out.writeUTF(include.pattern());
		}


		@Override
		public void readExternal(ObjectInput in) throws IOException
		{
			gzip = in.readBoolean();
			include = Pattern.compile(in.readUTF());
		}


		public boolean gzipped()
		{
			return gzip;
		}


		public void setGzip(boolean gzip)
		{
			this.gzip = gzip;
		}


		public Pattern include()
		{
			return include;
		}


		public void setInclude(Pattern include)
		{
			this.include = include;
		}

	}

	private SitemapOptions options = new SitemapOptions();
	URI base;
	Path sourceDir;
	Path targetDir;


	SitemapParameters()
	{}


	public SitemapParameters(URI base, Path sourcePath, Path targetPath)
	{
		this.base = ensureTrailingSlash(base);
		sourceDir = sourcePath;
		targetDir = targetPath;
	}


	private static URI ensureTrailingSlash(URI uri)
	{
		String yuri = uri.toString();
		return yuri.charAt(yuri.length() - 1) == '/' ? uri : Exceptional.uri(yuri + '/');
	}


	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		options.writeExternal(out);
		out.writeUTF(base.toString());
		out.writeUTF(sourceDir.toAbsolutePath().toString());
		out.writeUTF(targetDir.toAbsolutePath().toString());
	}


	@Override
	public void readExternal(ObjectInput in) throws IOException
	{
		options.readExternal(in);
		base = uri(in.readUTF());
		sourceDir = Paths.get(in.readUTF());
		targetDir = Paths.get(in.readUTF());
	}


	public SitemapOptions options()
	{
		return options;
	}


	public void serialize(@WillClose OutputStream out)
	{
		Closing.closeAfterAccepting(ObjectOutputStream::new, out, this::writeExternal);
	}


	public SitemapParameters deserialize(@WillClose InputStream in)
	{
		Closing.closeAfterAccepting(ObjectInputStream::new, in, this::readExternal);
		return this;
	}
}
