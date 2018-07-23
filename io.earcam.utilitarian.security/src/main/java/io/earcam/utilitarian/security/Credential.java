/*-
 * #%L
 * io.earcam.utilitarian.security
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
package io.earcam.utilitarian.security;

import java.util.Arrays;
import java.util.Objects;

import javax.security.auth.Destroyable;

/**
 * Implements {@link AutoCloseable} to provoke compiler warnings, where the
 * implementation of {@link #close()} calls {@link #destroy()}
 *
 */
public class Credential implements Destroyable, AutoCloseable {

	private String name;
	private char[] password;


	public Credential(String name, char[] password)
	{
		Objects.requireNonNull(name, "name");
		this.name = name;
		this.password = password;
	}


	@Override
	public void destroy()
	{
		name = null;
		Arrays.fill(password, ' ');
		password = null;
	}


	@Override
	public boolean isDestroyed()
	{
		return name == null;
	}


	public String name()
	{
		return name;
	}


	public char[] password()
	{
		return password;
	}


	@Override
	public void close()
	{
		destroy();
	}
}
