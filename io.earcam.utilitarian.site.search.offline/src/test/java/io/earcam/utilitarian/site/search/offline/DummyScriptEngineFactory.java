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

import java.io.Reader;
import java.util.List;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

public class DummyScriptEngineFactory implements ScriptEngineFactory {

	public static class DummyScriptEngine extends AbstractScriptEngine implements Invocable {

		@Override
		public Object eval(String script, ScriptContext context) throws ScriptException
		{
			return "dummy";
		}


		@Override
		public Object eval(Reader reader, ScriptContext context) throws ScriptException
		{
			return "dummy";
		}


		@Override
		public Bindings createBindings()
		{
			throw new UnsupportedOperationException("dummy");
		}


		@Override
		public ScriptEngineFactory getFactory()
		{
			throw new UnsupportedOperationException("dummy");
		}


		@Override
		public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException
		{
			throw new UnsupportedOperationException("dummy");
		}


		@Override
		public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException
		{
			throw new UnsupportedOperationException("dummy");
		}


		@Override
		public <T> T getInterface(Class<T> clasz)
		{
			throw new UnsupportedOperationException("dummy");
		}


		@Override
		public <T> T getInterface(Object thiz, Class<T> clasz)
		{
			throw new UnsupportedOperationException("dummy");
		}
	}


	@Override
	public String getEngineName()
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public String getEngineVersion()
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public List<String> getExtensions()
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public List<String> getMimeTypes()
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public List<String> getNames()
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public String getLanguageName()
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public String getLanguageVersion()
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public Object getParameter(String key)
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public String getMethodCallSyntax(String obj, String m, String... args)
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public String getOutputStatement(String toDisplay)
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public String getProgram(String... statements)
	{
		throw new UnsupportedOperationException("dummy");
	}


	@Override
	public ScriptEngine getScriptEngine()
	{
		return new DummyScriptEngine();
	}
}