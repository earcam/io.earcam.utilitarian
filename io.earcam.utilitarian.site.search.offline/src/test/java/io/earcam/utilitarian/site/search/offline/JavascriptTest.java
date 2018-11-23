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

import static io.earcam.unexceptional.Exceptional.unwrap;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.earcam.unexceptional.UncheckedException;
import io.earcam.unexceptional.UncheckedReflectiveException;
import io.earcam.utilitarian.site.search.offline.DummyScriptEngineFactory.DummyScriptEngine;

public class JavascriptTest {

	@Test
	public void cannotConstructWithoutObjenesis() throws Exception
	{
		Constructor<Javascript> constructor = Javascript.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		try {
			constructor.newInstance();
			fail();
		} catch(InvocationTargetException e) {
			assertThat(unwrap(e), is(instanceOf(IllegalStateException.class)));
		}
	}


	@Test
	public void throwsForInvalidScript()
	{
		ByteArrayInputStream input = new ByteArrayInputStream("This is not valid javascript".getBytes(UTF_8));
		try {
			Javascript.createJavascriptEngine(input);
			fail();
		} catch(ScriptRuntimeException e) {}
	}


	@Test
	public void throwsWhenFunctionInvocationThrows()
	{
		ByteArrayInputStream input = new ByteArrayInputStream("function bang(){ throw 'error: oh noes' }".getBytes(UTF_8));
		Invocable engine = Javascript.createJavascriptEngine(input);
		try {
			Javascript.invokeFunction(engine, "bang");
			fail();
		} catch(UncheckedException e) {
			assertThat(e.getCause(), is(instanceOf(ScriptException.class)));
		}

	}


	@Test
	public void throwsWhenScriptEngineNotFoundForLanguage()
	{
		ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
		try {
			Javascript.createScriptEngine("NoScriptingLanguageWithThisNameShouldEverExistSeriously", input);
			fail();
		} catch(ScriptRuntimeException e) {}
	}

	@Nested
	public class UseScriptEngineProperty {

		@Test
		void throwsWhenAlternativeScriptEngineClassCannotBeFound()
		{
			try {
				System.setProperty(Resources.PROPERTY_USE_SCRIPT_ENGINE, "for.this.static.final.default.class.NotFound");
				ByteArrayInputStream input = new ByteArrayInputStream("function valid(){}".getBytes(UTF_8));
				Javascript.createJavascriptEngine(input);
				fail();
			} catch(UncheckedReflectiveException e) {} finally {
				System.setProperty(Resources.PROPERTY_USE_SCRIPT_ENGINE, "");
			}
		}


		@Test
		void alternativeScriptEngineUsed()
		{
			try {
				System.setProperty(Resources.PROPERTY_USE_SCRIPT_ENGINE, DummyScriptEngineFactory.class.getCanonicalName());
				ByteArrayInputStream input = new ByteArrayInputStream("function valid(){}".getBytes(UTF_8));
				Invocable engine = Javascript.createJavascriptEngine(input);

				assertThat(engine, is(instanceOf(DummyScriptEngine.class)));

			} finally {
				System.setProperty(Resources.PROPERTY_USE_SCRIPT_ENGINE, "");
			}
		}
	}
}
