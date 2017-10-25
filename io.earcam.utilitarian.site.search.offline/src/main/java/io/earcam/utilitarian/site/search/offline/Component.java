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

import java.nio.charset.Charset;
import java.util.Map;

public interface Component {

	public default String id()
	{
		return this.getClass().getSimpleName();
	}


	public default void configure(Map<String, String> configuration)
	{}


	public static String mandatory(Map<String, String> configuration, String key)
	{
		if(configuration.containsKey(key)) {
			return configuration.get(key);
		}
		throw new NullPointerException("Missing mandatory configuration: " + key);
	}


	public static boolean getOrDefault(Map<String, String> configuration, String key, boolean defaultValue)
	{
		return Boolean.parseBoolean(configuration.getOrDefault(key, Boolean.toString(defaultValue)));
	}


	public static Charset getOrDefault(Map<String, String> configuration, String key, Charset defaultValue)
	{
		return Charset.forName(configuration.getOrDefault(key, defaultValue.toString()));
	}
}
