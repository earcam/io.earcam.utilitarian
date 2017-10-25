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

import java.net.URI;

import javax.json.bind.adapter.JsonbAdapter;

import io.earcam.unexceptional.Exceptional;

public class JsonbUriAdapter implements JsonbAdapter<URI, String> {

	@Override
	public String adaptToJson(URI uri)
	{
		return uri.toString();
	}


	@Override
	public URI adaptFromJson(String jsonValue)
	{
		return Exceptional.uri(jsonValue);
	}
}
