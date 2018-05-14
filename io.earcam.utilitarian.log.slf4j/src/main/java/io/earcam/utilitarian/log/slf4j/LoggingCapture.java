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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.impl.SimpleLogger.LOG_FILE_KEY;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import io.earcam.unexceptional.Exceptional;

final class LoggingCapture {

	private LoggingCapture()
	{}


	public static String capture(LogCapturable runnable)
	{
		ByteArrayOutputStream capture = new ByteArrayOutputStream();
		capture(runnable, capture);
		return new String(capture.toByteArray(), charset());
	}


	private static void capture(LogCapturable runnable, ByteArrayOutputStream capture)
	{
		PrintStream original = getPrintStream();
		try {                                            // NOSONAR
			setPrintStream(Exceptional.apply(LoggingCapture::newPrintStream, capture));
			execute(runnable);
		} finally {
			if(original != null) {
				setPrintStream(original);
			}
		}
	}


	private static PrintStream newPrintStream(OutputStream capture) throws UnsupportedEncodingException
	{
		return new PrintStream(capture, true, charset().toString());
	}


	private static void execute(LogCapturable runnable)
	{
		Exceptional.run(runnable::run);
	}


	private static PrintStream getPrintStream()
	{
		return useStdOut() ? System.out : System.err;    // NOSONAR no!
	}


	static boolean useStdOut()
	{
		return "System.out".equals(System.getProperty(LOG_FILE_KEY, "System.err"));
	}


	private static void setPrintStream(PrintStream printStream)
	{
		if(useStdOut()) {
			System.setOut(printStream);
		} else {
			System.setErr(printStream);
		}
	}


	private static Charset charset()
	{
		return Charset.forName(System.getProperty("file.encoding", UTF_8.name()));
	}
}
