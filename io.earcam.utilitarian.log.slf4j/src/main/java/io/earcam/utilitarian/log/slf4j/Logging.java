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

import static io.earcam.unexceptional.Exceptional.swallow;
import static io.earcam.utilitarian.log.slf4j.Constants.SYSTEM_PREFIX;

/**
 * <p>
 * Programmatic configuration for SLF4J logging
 * </p>
 *
 *
 * http://docs.jboss.org/hibernate/orm/4.3/topical/html/logging/Logging.html
 * https://www.eclipse.org/jetty/documentation/9.3.x/configuring-logging.html
 * http://docs.hazelcast.org/docs/3.5/manual/html/logging.html
 * http://cxf.apache.org/docs/general-cxf-logging.html
 * https://wiki.eclipse.org/EclipseLink/Development/296391
 */
public final class Logging implements LoggingBuilder, LoggerName {

	private static final String CACHE_OUTPUT_STREAM_STRING_KEY = SYSTEM_PREFIX + "cacheOutputStream";

	private static final String LOGGER_PROPERTY_CXF = "org.apache.cxf.Logger";
	private static final String LOGGER_IMP_CXF = "org.apache.cxf.common.logging.Slf4jLogger";

	private static final String LOGGER_PROPERTY_JETTY = "org.eclipse.jetty.util.log.class";
	private static final String LOGGER_IMP_JETTY = "org.eclipse.jetty.util.log.Slf4jLog";

	private static final String LOGGER_PROPERTY_ECLIPSELINK = "eclipselink.logging.logger";
	private static final String LOGGER_IMP_ECLIPSELINK = "org.eclipse.persistence.logging.slf4j.SLF4JLogger";

	private static final String SLF_4_YAY = "slf4j";

	private static final String LOGGER_PROPERTY_HAZELCAST = "hazelcast.logging.type";
	private static final String LOGGER_PROPERTY_JBOSS = "org.jboss.logging.provider";


	private Logging()
	{}


	public static LoggingBuilder logging()
	{
		return new Logging();
	}


	@Override
	public LogAtLevel log(String loggerName)
	{
		return Levels.create().log(loggerName);
	}


	@Override
	public LoggingBuilder defaultLevel(Level defaultLevel)
	{
		Levels.createWithDefault(defaultLevel);
		return this;
	}


	@Override
	public LoggingBuilder configureFrameworks()
	{
		System.setProperty(CACHE_OUTPUT_STREAM_STRING_KEY, Boolean.toString(false));

		System.setProperty(LOGGER_PROPERTY_JBOSS, SLF_4_YAY);
		System.setProperty(LOGGER_PROPERTY_HAZELCAST, SLF_4_YAY);

		setPropertyIfClassPresent(LOGGER_PROPERTY_JETTY, LOGGER_IMP_JETTY);
		setPropertyIfClassPresent(LOGGER_PROPERTY_ECLIPSELINK, LOGGER_IMP_ECLIPSELINK);
		setPropertyIfClassPresent(LOGGER_PROPERTY_CXF, LOGGER_IMP_CXF);

		return this;
	}


	private static void setPropertyIfClassPresent(String property, String implementation)
	{
		try {
			Logging.class.getClassLoader().loadClass(implementation);
			System.setProperty(property, implementation);
		} catch(ClassNotFoundException e) {
			swallow(e);
		}
	}


	/**
	 * If you're reliant on log output for test assertions then something is probably wrong.
	 * That said, cases (such as refactoring age-old legacy code) do exist
	 *
	 * @param runnable
	 * @return the captured log output
	 */
	public static String capture(LogCapturable runnable)
	{
		return LoggingCapture.capture(runnable);
	}
}
