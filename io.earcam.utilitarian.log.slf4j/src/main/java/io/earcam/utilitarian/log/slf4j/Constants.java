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

final class Constants {

	static final String SYSTEM_PREFIX = "org.slf4j.simpleLogger.";
	static final String DEFAULT_LOG_LEVEL_KEY = SYSTEM_PREFIX + "defaultLogLevel";
	static final String LOG_KEY_PREFIX = SYSTEM_PREFIX + "log.";
	static final String LOG_FILE_KEY = SYSTEM_PREFIX + "logFile";


	private Constants()
	{}
}
