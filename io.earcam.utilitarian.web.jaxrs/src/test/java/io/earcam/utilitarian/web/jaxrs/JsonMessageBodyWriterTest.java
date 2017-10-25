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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

import com.acme.Echo;

public class JsonMessageBodyWriterTest {

	@Test
	public void symmetric()
	{
		JsonMessageBodyWriter writer = new JsonMessageBodyWriter();

		Echo instance = new Echo("original", "echoed");

		MultivaluedMap<String, Object> outputHeaders = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		writer.writeTo(instance, Echo.class, Echo.class, null, APPLICATION_JSON_TYPE, outputHeaders, output);

		ByteArrayInputStream in = new ByteArrayInputStream(output.toByteArray());

		Annotation[] annos = null;
		MultivaluedMap<String, String> inputHeaders = null;
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Echo read = (Echo) new JsonMessageBodyReader().readFrom((Class) Echo.class, Echo.class, annos, APPLICATION_JSON_TYPE, inputHeaders, in);

		assertThat(read.getOriginal(), is(equalTo(instance.getOriginal())));
		assertThat(read.getEchoed(), is(equalTo(instance.getEchoed())));
	}


	@Test
	public void throwsUnchecked()
	{
		JsonMessageBodyWriter writer = new JsonMessageBodyWriter();

		Echo instance = new Echo("original", "echoed");

		MultivaluedMap<String, Object> outputHeaders = null;

		OutputStream output = new OutputStream() {
			@Override
			public void write(int b) throws IOException
			{
				throw new IOException("Put out nothing");
			}
		};

		try {
			writer.writeTo(instance, Echo.class, Echo.class, null, APPLICATION_JSON_TYPE, outputHeaders, output);
			fail();
		} catch(RuntimeException e) {}
	}


	@Test
	public void getSizeIsMinusOne()
	{
		JsonMessageBodyWriter writer = new JsonMessageBodyWriter();
		long size = writer.getSize(new Object(), Object.class, Object.class, null, APPLICATION_JSON_TYPE);

		assertThat(size, is(-1L));
	}
}
