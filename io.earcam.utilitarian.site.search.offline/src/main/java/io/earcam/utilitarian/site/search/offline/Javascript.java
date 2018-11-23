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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.ServiceLoader.load;
import static java.util.stream.StreamSupport.stream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Optional;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import io.earcam.unexceptional.Exceptional;

final class Javascript {

	private static final String ECMA_SCRIPT = "ECMAScript";
	private static final Charset CHARSET = UTF_8;


	private Javascript()
	{
		throw new IllegalStateException("nope");
	}


	public static Invocable createJavascriptEngine(InputStream... scripts)
	{
		return createScriptEngine(ECMA_SCRIPT, scripts);
	}


	public static Invocable createScriptEngine(String language, InputStream... scripts)
	{
		ScriptEngine engine = specified(language).orElseGet(() -> spi(language));

		for(InputStream script : scripts) {
			try {
				engine.eval(new InputStreamReader(script, CHARSET));
			} catch(ScriptException e) {
				throw new ScriptRuntimeException(e);
			}
		}
		return (Invocable) engine;
	}


	private static Optional<ScriptEngine> specified(String language)
	{
		String specified = System.getProperty(Resources.PROPERTY_USE_SCRIPT_ENGINE, "");

		return ("".equals(specified)) ? Optional.empty() : Exceptional.apply(Javascript::loadSpecified, specified, language);
	}


	private static Optional<ScriptEngine> loadSpecified(String engineType, String language) throws ReflectiveOperationException
	{
		Class<?> specific = Javascript.class.getClassLoader().loadClass(engineType);
		ScriptEngineFactory factory = (ScriptEngineFactory) specific.newInstance();
		return Optional.of(factory.getScriptEngine());
	}


	private static ScriptEngine spi(String language)
	{
		return stream(load(ScriptEngineFactory.class).spliterator(), false)
				.filter(f -> f.getLanguageName().equals(language))
				.findAny()
				.orElseThrow(ScriptRuntimeException::engineNotFound)
				.getScriptEngine();
	}


	public static Object invokeFunction(Invocable engine, String name, Object... args)
	{
		return Exceptional.apply(engine::invokeFunction, name, args);
	}
}