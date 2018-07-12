/*-
 * #%L
 * io.earcam.utilitarian.io
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
package io.earcam.utilitarian.io;

//EARCAM_SNIPPET_BEGIN: imports
import static io.earcam.utilitarian.io.SplittableOutputStream.splittable;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
//EARCAM_SNIPPET_END: imports

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

public class SplittableOutputStreamTest {

	// Given content fits exactly, then all is written and new OutputStream is returned
	@Test
	public void contentWithOneRecordFitsMaxSizeExactly() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();;
		ByteArrayOutputStream unused = new ByteArrayOutputStream();
		Supplier<OutputStream> supplier = Arrays.asList(out, unused).iterator()::next;

		try(SplittableOutputStream splittable = splittable(supplier, bytes("HEAD"), bytes("FOOT")).maxSize(12).outputStream()) {

			splittable.beginRecord();
			splittable.write(bytes("BODY"));
			splittable.endRecord();
		}
		assertThat(new String(out.toByteArray(), UTF_8), is(equalTo("HEADBODYFOOT")));
		assertThat(unused.toByteArray().length, is(0));
	}


	private static byte[] bytes(String text)
	{
		return text.getBytes(UTF_8);
	}


	// Given content fits exactly, then only head and record is written and same OutputStream is returned
	@Test
	public void contentWithOneRecordAndSpaceTo() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Supplier<OutputStream> supplier = Arrays.asList(out, null).iterator()::next;

		try(SplittableOutputStream splittable = splittable(supplier, bytes("HEAD"), bytes("FOOT")).maxSize(13).outputStream()) {

			splittable.beginRecord();
			splittable.write(bytes("BODY"));
			splittable.endRecord();
			assertThat(new String(out.toByteArray(), UTF_8), is(equalTo("HEADBODY")));
		}
		assertThat(new String(out.toByteArray(), UTF_8), is(equalTo("HEADBODYFOOT")));
	}


	// Given content fits exactly, then all is written and new OutputStream is returned.
	// the new {@link OutputStream} has no content.
	@Test
	public void contentWithTwoRecordsFitsMaxSizeExactly() throws Exception
	{
		List<ByteArrayOutputStream> outputs = Arrays.asList(new ByteArrayOutputStream(), new ByteArrayOutputStream());
		Supplier<OutputStream> supplier = outputs.iterator()::next;

		try(SplittableOutputStream splittable = splittable(supplier, bytes("HEAD"), bytes("FOOT")).maxSize(16).outputStream()) {

			splittable.beginRecord();
			splittable.write(bytes("BODY"));
			splittable.endRecord();

			splittable.beginRecord();
			splittable.write('B');
			splittable.write('O');
			splittable.write('D');
			splittable.write('Y');
			splittable.endRecord();
		}
		assertThat(new String(outputs.get(0).toByteArray(), UTF_8), is(equalTo("HEADBODYBODYFOOT")));
		assertThat(new String(outputs.get(1).toByteArray(), UTF_8), is(equalTo("")));
	}


	// split criteria: either maxSize OR maxCount are met
	@Test
	public void contentWithTwoRecordsFitsMaxSizeExactlyIsSplitWhenMaxRecordsCountIsOne() throws Exception
	{
		List<ByteArrayOutputStream> outputs = Arrays.asList(new ByteArrayOutputStream(), new ByteArrayOutputStream(), new ByteArrayOutputStream());
		Supplier<OutputStream> supplier = outputs.iterator()::next;

		SplittableOutputStream splittable = splittable(supplier, bytes("HEAD"), bytes("FOOT")).maxSize(16).maxCount(1).outputStream();

		splittable.beginRecord();
		splittable.write(bytes("BODY"));
		splittable.endRecord();

		splittable.beginRecord();
		splittable.write('B');
		splittable.write('O');
		splittable.write('D');
		splittable.write('Y');
		splittable.endRecord();

		splittable.close();

		assertThat(new String(outputs.get(0).toByteArray(), UTF_8), is(equalTo("HEADBODYFOOT")));
		assertThat(new String(outputs.get(1).toByteArray(), UTF_8), is(equalTo("HEADBODYFOOT")));
	}


	@Test
	public void sizeOfheadPlusFootCannotExceedMaxSize() throws IOException
	{
		try {
			splittable(ByteArrayOutputStream::new, new byte[42], new byte[42]).maxSize(81).outputStream();
			fail();
		} catch(IllegalArgumentException e) {}
	}


	@Test
	public void cannotWriteByteArrayUnlessStarted() throws IOException
	{
		SplittableOutputStream output = splittable(ByteArrayOutputStream::new, new byte[21], new byte[42]).maxSize(1001).outputStream();
		try {
			output.write(bytes("hello"));
			fail();
		} catch(IllegalStateException e) {}
	}


	@Test
	public void cannotWriteByteUnlessStarted() throws IOException
	{
		SplittableOutputStream output = splittable(ByteArrayOutputStream::new, new byte[21], new byte[42]).maxSize(1001).outputStream();
		try {
			output.write(10);
			fail();
		} catch(IllegalStateException e) {}
	}


	@Test
	public void whenRecordSizePlusHeadAndFootExceedsMaxSizeThenThrowsBufferOverflow() throws IOException
	{
		SplittableOutputStream output = splittable(ByteArrayOutputStream::new, bytes("<html>"), bytes("</html>")).maxSize(14).outputStream();
		try {
			output.beginRecord();
			output.write(bytes("<head><title>oh noes</title></head>"));
			output.endRecord();
			fail();
		} catch(BufferOverflowException e) {}
	}


	@Test
	public void invokingEndWhenNotStartedThrowsIllegalState() throws Exception
	{
		SplittableOutputStream output = splittable(ByteArrayOutputStream::new, new byte[21], new byte[42]).maxSize(1001).outputStream();
		try {
			output.endRecord();
			fail();
		} catch(IllegalStateException e) {}
	}


	@Test
	public void invokingStartWhenAlreadyStartedThrowsIllegalState() throws Exception
	{
		SplittableOutputStream output = splittable(ByteArrayOutputStream::new, new byte[21], new byte[42]).maxSize(1001).outputStream();
		try {
			output.beginRecord();
			output.beginRecord();
			fail();
		} catch(IllegalStateException e) {}
	}


	@Test
	public void maxSizeCannotBeZero() throws Exception
	{
		try {
			splittable(() -> {
				throw new RuntimeException("supplied");
			}, new byte[21], new byte[42]).maxSize(0);
			fail();
		} catch(IllegalArgumentException e) {}
	}


	@Test
	public void maxRecordsCannotBeZero() throws Exception
	{
		try {
			splittable(() -> {
				throw new RuntimeException("supplied");
			}, new byte[21], new byte[42]).maxCount(0);
			fail();
		} catch(IllegalArgumentException e) {}
	}


	@Test
	public void maxSizeCannotBeNegative() throws Exception
	{
		try {
			splittable(() -> {
				throw new RuntimeException("supplied");
			}, new byte[21], new byte[42]).maxSize(-42);
			fail();
		} catch(IllegalArgumentException e) {}
	}


	@Test
	public void maxRecordsCannotBeNegative() throws Exception
	{
		try {
			splittable(() -> {
				throw new RuntimeException("supplied");
			}, new byte[21], new byte[42]).maxCount(-21);
			fail();
		} catch(IllegalArgumentException e) {}
	}


	@Test
	public void whenNoRecordsAreWrittenThenSupplierIsNeverCalled() throws Exception
	{
		try(SplittableOutputStream output = splittable(() -> {
			throw new RuntimeException("supplied");
		}, new byte[21], new byte[42]).maxSize(1001).outputStream()) {
			output.beginRecord();
			output.endRecord();
		}
	}


	@Test
	public void givenRecordPartWrittenWhenClosedThenThrowsUnderflow() throws Exception
	{
		RuntimeException caught = null;
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		try(SplittableOutputStream output = splittable(() -> o, bytes("a"), bytes("c")).maxSize(1001).outputStream()) {
			output.beginRecord();
			output.write(bytes("b"));
		} catch(RuntimeException e) {
			caught = e;
		}
		assertThat(caught, is(instanceOf(BufferUnderflowException.class)));
	}

	// EARCAM_SNIPPET_BEGIN: examples
	class CountingOutputStreamSupplier implements Supplier<OutputStream> {

		final List<ByteArrayOutputStream> supplied = new ArrayList<>();


		@Override
		public OutputStream get()
		{
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			supplied.add(outputStream);
			return outputStream;
		}


		public List<ByteArrayOutputStream> supplied()
		{
			return supplied;
		}


		@Override
		public String toString()
		{
			return supplied.stream()
					.map(ByteArrayOutputStream::toByteArray)
					.map(b -> new String(b, UTF_8))
					.collect(joining("\n"));
		}
	}


	@Test
	public void exampleSingleExactSplit() throws Exception
	{
		byte[] head = bytes("<html><head><head><body><ul>");  // length: 28
		byte[] foot = bytes("</ul></body></html>");           // length: 19

		// listItem length: [10,12]
		// total body length for 100 listItems: 1092 = 9 * 10 + 90 * 11 + 1 * 12
		CountingOutputStreamSupplier supplier = new CountingOutputStreamSupplier();

		// expect a single file: 28 + 1092 + 19
		try(SplittableOutputStream output = splittable(supplier, head, foot).maxSize(28 + 1092 + 19).outputStream()) {
			writeOneToOneHundredAsListItems(output);
		}

		assertThat(supplier, wroteOneToOneHundredAsListItems());
		assertThat(supplier, hasSuppliedUsedOutputStreams(1));
	}


	private Matcher<CountingOutputStreamSupplier> hasSuppliedUsedOutputStreams(int expected)
	{
		return new TypeSafeMatcher<CountingOutputStreamSupplier>() {

			@Override
			public void describeTo(Description description)
			{}


			@Override
			protected boolean matchesSafely(CountingOutputStreamSupplier supplier)
			{
				List<ByteArrayOutputStream> supplied = supplier.supplied();
				return supplied.size() == expected;
			}

		};
	}


	private Matcher<CountingOutputStreamSupplier> wroteOneToOneHundredAsListItems()
	{
		return new TypeSafeMatcher<SplittableOutputStreamTest.CountingOutputStreamSupplier>() {

			@Override
			public void describeTo(Description description)
			{}


			@Override
			protected boolean matchesSafely(CountingOutputStreamSupplier supplier)
			{
				String allOutput = supplier.toString();
				for(int i = 1; i <= 100; i++) {
					if(!allOutput.contains(listItem(i))) {
						return false;
					}
				}
				return true;
			}
		};
	}


	private void writeOneToOneHundredAsListItems(SplittableOutputStream output) throws IOException
	{
		for(int i = 1; i <= 100; i++) {
			output.beginRecord();
			output.write(bytes(listItem(i)));
			output.endRecord();
		}
	}


	private String listItem(int i)
	{
		return "<li>" + i + "</li>";
	}


	@Test
	public void exampleTwoExactSplits() throws Exception
	{
		byte[] head = bytes("<html><head><head><body><ul>");  // length: 28
		byte[] foot = bytes("</ul></body></html>");           // length: 19

		// listItem length: [10,12]
		// total body length for 100 listItems: 1092 = 9 * 10 + 90 * 11 + 1 * 12
		CountingOutputStreamSupplier supplier = new CountingOutputStreamSupplier();

		// expect a two files: 28 + 1092/2 + 19
		try(SplittableOutputStream output = splittable(supplier, head, foot).maxSize(28 + 1092 / 2 + 12 + 19).outputStream()) {
			writeOneToOneHundredAsListItems(output);
		}

		assertThat(supplier, wroteOneToOneHundredAsListItems());
		assertThat(supplier, hasSuppliedUsedOutputStreams(2));
	}


	@Test
	public void exampleThreeUnevenSplits() throws Exception
	{
		byte[] head = bytes("<html><head><head><body><ul>");  // length: 28
		byte[] foot = bytes("</ul></body></html>");           // length: 19

		// listItem length: [10,12]
		// total body length for 100 listItems: 1092 = 9 * 10 + 90 * 11 + 1 * 12
		CountingOutputStreamSupplier supplier = new CountingOutputStreamSupplier();

		// expect a single file: 28 + 545 + 19
		try(SplittableOutputStream output = splittable(supplier, head, foot).maxSize(28 + 545 + 19).outputStream()) {
			writeOneToOneHundredAsListItems(output);
		}

		assertThat(supplier, wroteOneToOneHundredAsListItems());
		assertThat(supplier, hasSuppliedUsedOutputStreams(3));
	}
	// EARCAM_SNIPPET_END: examples
}
