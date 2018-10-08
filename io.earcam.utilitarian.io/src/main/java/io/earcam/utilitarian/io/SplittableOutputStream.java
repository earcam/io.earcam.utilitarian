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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.function.Supplier;

/**
 * <p>
 * Deals with structured (e.g. XML) or unstructured data.
 * </p>
 *
 * <p>
 * User code must invoke {@link #beginRecord()} before writing {@code byte}s, and subsequently delimit safe
 * splitting points by invoking {@link #endRecord()}. The number of {@code byte}s written between the
 * call to {@link #beginRecord()} and call to {@link #endRecord()} must not exceed
 * {@link #maxSize(long)} - ({@link #header}{@code .length} + {@link #footer}{@code .length})
 * </p>
 *
 * <p>
 * A <i>record</i> is defined as any {@code byte}s written between calls to {@link #beginRecord()} and
 * {@link #endRecord()}. Should the maximum file size be specified and the length of a single record (plus
 * header and footer) exceed the maximum then a {@link BufferOverflowException} is throw.
 *
 * <p>
 * Common usage would be splitting files, in this case the {@link Supplier} is expected to <i>keep
 * track</i> of output file names.
 * </p>
 *
 * <p>
 * <b>Please note limitation</b>; due to the use of {@link Long} internally, the maximum
 * size per-file is limited to {@value java.lang.Long#MAX_VALUE} bytes (which is
 * 9,223PB or 9,223,000,000GB) per split {@link OutputStream} .
 *
 */
@SuppressWarnings("squid:S4349") // Sonar: Not applicable IMO
public class SplittableOutputStream extends OutputStream implements SplittableOutputStreamBuilder, SplittableOutputStreamBuilder.SplitOutputStreamBuilder {

	private final Supplier<OutputStream> supplier;
	private final byte[] header;
	private final byte[] footer;
	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	private volatile OutputStream out = null;
	private long maxFileSize = Long.MAX_VALUE;
	private long maxRecordCount = Long.MAX_VALUE;
	private long bytesCount;
	private long recordsCount;

	private boolean inScope;


	private SplittableOutputStream(Supplier<OutputStream> supplier, byte[] head, byte[] footer)
	{
		this.supplier = supplier;
		this.header = head;
		this.footer = footer;
	}


	/**
	 * Begin building a {@link SplittableOutputStream}
	 *
	 * @param next a {@link Supplier} of the underlying {@link OutputStream}s
	 * @param header written at the start of each {@link OutputStream} (e.g. file header)
	 * @param footer written at the end of each {@link OutputStream} (e.g. file footer)
	 * @return the builder for further construction
	 * @throws IOException rethrows in the unlikely event the underlying ByteArrayOutputStream buffer does
	 */
	public static SplittableOutputStreamBuilder splittable(Supplier<OutputStream> next, byte[] head, byte[] footer) throws IOException
	{
		@SuppressWarnings("squid:S2095")  // false positive - it's being returned
		SplittableOutputStream splittable = new SplittableOutputStream(next, head, footer);
		splittable.reset();
		return splittable;
	}


	private void reset() throws IOException
	{
		bytesCount = recordsCount = 0L;
		byte[] bytes = buffer.toByteArray();
		buffer.reset();
		buffer.write(header);
		buffer.write(bytes);
	}


	@Override
	public SplitOutputStreamBuilder maxSize(long bytes)
	{
		maxFileSize = bytes;
		checkSanity(header, footer, maxFileSize);
		return this;
	}


	@Override
	public SplitOutputStreamBuilder maxCount(long numberOfRecords)
	{
		requireNaturalNumber(numberOfRecords);
		maxRecordCount = numberOfRecords;
		return this;
	}


	private void requireNaturalNumber(long number)
	{
		if(number <= 0) {
			throw new IllegalArgumentException("A positive, non-zero value is required.  Received: " + number);
		}
	}


	@Override
	public SplittableOutputStream outputStream() throws IOException
	{
		return this;
	}


	private static void checkSanity(byte[] head, byte[] foot, long maxFileSize)
	{
		if(head.length + foot.length > maxFileSize) {
			throw new IllegalArgumentException("header.length + footer.length > maxFileSize: " + head.length + " + " + foot.length + " > " + maxFileSize);
		}
	}


	@Override
	public void write(int b) throws IOException
	{
		checkBeforeWrite(1);
		buffer.write(b);
	}


	private void checkBeforeWrite(int pendingBytes)
	{
		if(!inScope) {
			throw new IllegalStateException("Record scope not started");
		}
		if(pendingBytes + header.length + footer.length > maxFileSize) {
			throw new BufferOverflowException();
		}
	}


	@Override
	public void write(byte[] bytes) throws IOException
	{
		checkBeforeWrite(bytes.length);
		buffer.write(bytes);
	}


	/**
	 * Called to mark the beginning of a <i>record</i> (where a "record" is any block
	 * of bytes that can only be treated atomically; in that it's valid to split content
	 * at the record's boundaries.
	 *
	 * @see #endRecord()
	 */
	public void beginRecord()
	{
		if(inScope) {
			throw new IllegalStateException("Record scope already started");
		}
		inScope = true;
	}


	/**
	 * Called to mark the end of a <i>record</i>
	 *
	 * @throws IOException rethrows anything from the underlying {@link OutputStream}
	 *
	 * @see #beginRecord()
	 */
	public void endRecord() throws IOException
	{
		endScope();
		++recordsCount;
		if(bufferIsTooLarge() || maxRecordsExceeded()) {
			endSplit();
		}
		if(recorded()) {
			writeBuffer();
		}
	}


	private void endScope()
	{
		if(!inScope) {
			throw new IllegalStateException("Record scope not started, cannot end");
		}
		inScope = false;
	}


	private boolean bufferIsTooLarge()
	{
		return bytesCount + footer.length + buffer.size() > maxFileSize;
	}


	private boolean maxRecordsExceeded()
	{
		return recordsCount > maxRecordCount;
	}


	private void endSplit() throws IOException
	{
		if(out != null) {
			out.write(footer);
			out.close();
			out = null;
			reset();
		}
	}


	private boolean recorded()
	{
		return (recordsCount == 1 && buffer.size() > header.length)
				|| (recordsCount != 1 && buffer.size() > 0);
	}


	private void writeBuffer() throws IOException
	{
		out().write(buffer.toByteArray());
		bytesCount += buffer.size();
		buffer.reset();
	}


	private OutputStream out()
	{
		if(out == null) {
			out = supplier.get();
		}
		return out;
	}


	@Override
	public void close() throws IOException
	{
		if(recorded()) {
			throw new BufferUnderflowException();
		}
		endSplit();
		if(buffer.size() > header.length) {
			writeBuffer();
			endSplit();
		}
	}
}
