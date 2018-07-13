/*-
 * #%L
 * io.earcam.utilitarian.log.slf4j
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
package io.earcam.utilitarian.log.slf4j;

import static io.earcam.utilitarian.log.slf4j.Constants.DEFAULT_LOG_LEVEL_KEY;
import static io.earcam.utilitarian.log.slf4j.Constants.LOG_KEY_PREFIX;

final class Levels implements LoggingBuilder.LogAtLevel, LoggerName {

	private String lastLoggerName;


	private Levels()
	{}


	/**
	 * Will not overwrite the {@value SimpleLogger#DEFAULT_LOG_LEVEL_KEY} system property if present
	 * (to allow external control from, e.g. build properties
	 *
	 * @param level
	 * @return
	 */
	static final LoggerName createWithDefault(Level level)
	{
		if(isNullOrEmpty(System.getProperty(DEFAULT_LOG_LEVEL_KEY))) {
			set(DEFAULT_LOG_LEVEL_KEY, level);
		}
		return create();
	}


	static final LoggerName create()
	{
		return new Levels();
	}


	private static boolean isNullOrEmpty(String property)
	{
		return property == null;
	}


	private static void set(String loggerCategory, Level level)
	{
		System.setProperty(loggerCategory, level.toString());
	}


	@Override
	public LoggingBuilder.LogAtLevel log(String loggerName)
	{
		lastLoggerName = loggerName;
		return this;
	}


	/**
	 * Can also be used to during execution to change levels
	 */
	@Override
	public LoggerName at(Level level)
	{
		set(LOG_KEY_PREFIX + lastLoggerName, level);
		return this;
	}
}
