/*-
 * #%L
 * io.earcam.utilitarian.web
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
package io.earcam.utilitarian.web.jaxrs;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.acme.Echo;

public class JsonMessageBodyReaderTest {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void throwsUnchecked()
	{
		String message = "Put in nothing";
		JsonMessageBodyReader reader = new JsonMessageBodyReader();

		InputStream input = new InputStream() {

			@Override
			public int read() throws IOException
			{
				throw new IOException(message);
			}
		};

		try {
			reader.readFrom((Class) Echo.class, Echo.class, null, MediaType.APPLICATION_JSON_TYPE, null, input);
			fail();
		} catch(RuntimeException e) {
			Throwable t = e;
			while(t.getCause() != null) {
				t = t.getCause();
			}
			assertThat(t, is(instanceOf(IOException.class)));
			assertThat(t.getMessage(), is(equalTo(message)));
		}
	}
}
