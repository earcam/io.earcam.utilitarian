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
package io.earcam.utilitarian.site.search.offline.jsonb;

import java.io.StringWriter;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

public final class JsonBind {

	private JsonBind()
	{}

	private static final JsonbConfig CONFIGURATION = new JsonbConfig()
			.withNullValues(true)
			.withFormatting(true)
			.withAdapters(
					new JsonbFileAdapter() {},
					new JsonbUriAdapter() {});

	private static final Jsonb JSONB = JsonbBuilder.create(CONFIGURATION);


	public static String writeJson(Object instance)
	{
		return writeJson(JSONB, instance);
	}


	public static String writeJson(Jsonb jsonb, Object instance)
	{
		StringWriter writer = new StringWriter();
		jsonb.toJson(instance, writer);
		return writer.toString();
	}


	public static <T> T readJson(String json, Class<T> type)
	{
		return JSONB.fromJson(json, type);
	}
}
