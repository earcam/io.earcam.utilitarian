/*-
 * #%L
 * io.earcam.utilitarian.file
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
package io.earcam.utilitarian.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import org.junit.Test;

public class DigestiveTest {

	@Test
	public void hexString() throws Exception
	{
		byte[] bytes = toByteArray(0xCafe_Babe);

		String hex = Digestive.toHexString(bytes);

		assertThat(hex, is(equalTo("CAFEBABE")));
	}


	@Test
	public void sha1()
	{
		ByteArrayInputStream input = new ByteArrayInputStream("ABCDEF".getBytes(UTF_8));

		String hex = Digestive.sha1Hex(input);

		assertThat(hex, is(equalTo("970093678B182127F60BB51B8AF2C94D539ECA3A")));
	}


	private byte[] toByteArray(int value)
	{
		return ByteBuffer.allocate(4).putInt(value).array();
	}


	@Test
	public void noSuchAlgorithmThrownAsIllegalArgument()
	{
		try {
			Digestive.digestAlgorithm("SHA SHA Rasputin lover of the Russian... digest?");
			fail();
		} catch(IllegalArgumentException e) {}
	}


	@Test
	public void sha1InputStreamThrowsUncheckedIO()
	{
		try {
			Digestive.sha1(new InputStream() {
				public int read() throws IOException
				{
					throw new IOException("No surprise");
				}
			});
			fail();
		} catch(UncheckedIOException e) {}
	}

}
