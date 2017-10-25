/*-
 * #%L
 * io.earcam.utilitarian.net
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
package io.earcam.utilitarian.net;

import static io.earcam.unexceptional.Exceptional.unwrap;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;

import org.junit.Test;

public class FreePortFinderTest {

	@Test
	public void cannotConstructWithoutObjenesis() throws Exception
	{
		Constructor<FreePortFinder> constructor = FreePortFinder.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		try {
			constructor.newInstance();
			fail();
		} catch(InvocationTargetException e) {
			assertThat(unwrap(e), is(instanceOf(IllegalStateException.class)));
		}
	}


	@Test
	public void testName() throws IOException
	{
		int freePort = FreePortFinder.findFreePort();

		try(ServerSocket socket = new ServerSocket(freePort)) {}

		assertThat(freePort, is(not(0)));
	}
}
