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

// EARCAM_SNIPPET_BEGIN: imports
import static io.earcam.utilitarian.log.slf4j.Level.DEBUG;
import static io.earcam.utilitarian.log.slf4j.Level.ERROR;
import static io.earcam.utilitarian.log.slf4j.Level.INFO;
import static io.earcam.utilitarian.log.slf4j.Level.TRACE;
import static io.earcam.utilitarian.log.slf4j.Logging.logging;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
// EARCAM_SNIPPET_END: imports

import java.lang.reflect.Method;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.simple.SimpleLogger;

import io.earcam.unexceptional.Exceptional;

@NotThreadSafe
public class LoggingTest {

	@Test
	public void individualLogLevelsAreHonoured()
	{
		// EARCAM_SNIPPET_BEGIN: initialize
		logging()
				.configureFrameworks()
				.defaultLevel(INFO)
				.log("com.acme").at(DEBUG)
				.log(getClass()).at(TRACE)
				.log("com.annoying").at(ERROR);
		// EARCAM_SNIPPET_END: initialize

		Logger acmeLogger = LoggerFactory.getLogger("com.acme");
		Logger annoyingLogger = LoggerFactory.getLogger("com.annoying");

		String doNotDisplay = "shouldn't see me";
		String doDisplay = "should see me";

		String captured = Logging.capture(() -> {

			acmeLogger.trace(doNotDisplay);
			acmeLogger.debug(doDisplay);

			annoyingLogger.warn(doNotDisplay);
			annoyingLogger.error(doDisplay);
		});

		assertThat(captured, containsString(doDisplay));
		assertThat(captured, not(containsString(doNotDisplay)));
	}


	@Test
	public void basicCaptureOverStdErr()
	{
		forceSimpleLoggerResetTo("System.err");

		logging()
				.configureFrameworks()
				.defaultLevel(INFO)
				.log("com.acme.std.err").at(DEBUG);

		// EARCAM_SNIPPET_BEGIN: capture
		Logger acmeLogger = LoggerFactory.getLogger("com.acme");

		String wee = "Weeeeeeeeeee!";
		String captured = Logging.capture(() -> acmeLogger.info(wee));
		assertThat(captured, containsString(wee));
		// EARCAM_SNIPPET_END: capture
	}


	private void forceSimpleLoggerResetTo(String output)
	{
		System.setProperty(Constants.LOG_FILE_KEY, output);
		try {
			Method init = SimpleLogger.class.getDeclaredMethod("init");
			init.setAccessible(true);
			init.invoke(null);
		} catch(SecurityException | IllegalArgumentException | ReflectiveOperationException e) {
			Exceptional.rethrow(e);
		}
	}


	@Test
	public void basicCaptureOverStdOut()
	{
		forceSimpleLoggerResetTo("System.out");

		logging()
				.configureFrameworks()
				.defaultLevel(INFO)
				.log("com.acme.std.out").at(DEBUG);

		Logger acmeLogger = LoggerFactory.getLogger("com.acme");

		String wee = "Weeeeeeeeeee!";
		String captured = Logging.capture(() -> acmeLogger.info(wee));
		assertThat(captured, containsString(wee));
	}
}
