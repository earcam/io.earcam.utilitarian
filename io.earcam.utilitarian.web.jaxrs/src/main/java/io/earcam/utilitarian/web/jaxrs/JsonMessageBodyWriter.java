/*-
 * #%L
 * io.earcam.utilitarian.web.jaxrs
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

import static io.earcam.utilitarian.web.jaxrs.JsonMessageBodyReader.JSONB;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import io.earcam.unexceptional.Exceptional;

@Produces(APPLICATION_JSON)
@Provider
public class JsonMessageBodyWriter implements MessageBodyWriter<Object> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return true;
	}


	@Override
	public long getSize(Object instance, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return -1;
	}


	@Override
	public void writeTo(Object instance, Class<?> type,
			Type gt, Annotation[] anno, MediaType mt,
			MultivaluedMap<String, Object> h, OutputStream entityStream)
	{
		Exceptional.run(() -> JSONB.toJson(instance, gt, entityStream));
	}
}
