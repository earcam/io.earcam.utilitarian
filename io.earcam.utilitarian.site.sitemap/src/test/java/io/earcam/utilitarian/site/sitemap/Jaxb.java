/*-
 * #%L
 * io.earcam.utilitarian.site.sitemap
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
package io.earcam.utilitarian.site.sitemap;

import static javax.xml.bind.JAXBContext.newInstance;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import io.earcam.unexceptional.Exceptional;

final class Jaxb {

	protected static final String INDENT_STRING = "com.sun.xml.internal.bind.indentString";


	private Jaxb()
	{}


	public static String marshal(Object instance)
	{
		return Exceptional.apply(Jaxb::checkedMarshal, instance, "");
	}


	public static String marshal(Object instance, String prettyPrintIndent)
	{
		requireNonEmpty(prettyPrintIndent);
		return Exceptional.apply(Jaxb::checkedMarshal, instance, prettyPrintIndent);
	}


	private static void requireNonEmpty(String prettyPrintIndent)
	{
		Objects.requireNonNull(prettyPrintIndent);
		if("".equals(prettyPrintIndent)) {
			throw new IllegalArgumentException("prettyPrintIndent cannot be an empty String");
		}
	}


	private static String checkedMarshal(Object instance, String prettyPrintIndent) throws JAXBException
	{
		StringWriter writer = new StringWriter();
		marshaller(instance, prettyPrintIndent).marshal(instance, writer);
		return writer.toString();
	}


	private static Marshaller marshaller(Object instance, String prettyPrintIndent) throws JAXBException
	{
		Marshaller marshaller = newInstance(instance.getClass()).createMarshaller();
		if(prettyPrintIndent != null) {
			configurePrettyPrint(marshaller, prettyPrintIndent);
		}
		return marshaller;
	}


	public static void marshal(Object instance, String prettyPrintIndent, OutputStream output)
	{
		Exceptional.run(() -> checkedMarshal(instance, prettyPrintIndent, output));
	}


	private static void checkedMarshal(Object instance, String prettyPrintIndent, OutputStream output) throws JAXBException, FileNotFoundException
	{
		Marshaller marshaller = marshaller(instance, prettyPrintIndent);

		marshaller.marshal(instance, output);
	}


	private static void configurePrettyPrint(Marshaller marshaller, String prettyPrintIndent) throws PropertyException
	{
		marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(INDENT_STRING, prettyPrintIndent);
		if(!prettyPrintIndent.equals(marshaller.getProperty(INDENT_STRING))) {
			throw new IllegalStateException(
					"Unable to set the indent string property '" + INDENT_STRING
							+ "' so length calculations will fail. Disable pretty printing to circumvent.");
		}
	}


	public static <T> T unmarshal(String xml, Class<T> type)
	{
		return Exceptional.apply(Jaxb::checkedUnmarshal, xml, type);
	}


	private static <T> T checkedUnmarshal(String xml, Class<T> type) throws JAXBException
	{
		return type.cast(unmarshaller(type).unmarshal(new StringReader(xml)));
	}


	public static Unmarshaller unmarshaller(Class<?> type) throws JAXBException
	{
		return newInstance(type).createUnmarshaller();
	}
}
