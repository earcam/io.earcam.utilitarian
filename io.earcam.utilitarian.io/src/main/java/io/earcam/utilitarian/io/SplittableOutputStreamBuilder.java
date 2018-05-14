/*-
 * #%L
 * io.earcam.utilitarian.file
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Create a {@link SplittableOutputStream} with maximum byte size and/or maximum number of records
 */
public interface SplittableOutputStreamBuilder {

	public interface SplitOutputStreamBuilder extends SplittableOutputStreamBuilder {

		/**
		 * <p>
		 * The builder's build method.
		 * </p>
		 * <p>
		 * <b>Note: side effect</b>; also writes the <b>head</b> {@code byte} array.
		 * </p>
		 *
		 * @return An {@link OutputStream} for regular use
		 * @throws IOException if the attempt to write the <b>head</b> fails
		 */
		public abstract SplittableOutputStream outputStream() throws IOException;

	}


	/**
	 * <p>
	 * Split criteria for maximum file size.
	 * </p>
	 *
	 * <p>
	 * May also be composed with maximum record count criteria.
	 * </p>
	 *
	 * @param fileSizeBytes the maximum file size permitted.
	 * @return the builder for further construction
	 * @throws IllegalArgumentException if {@code fileSizeBytes < head.length + tail.length}
	 *
	 * @see #maxCount(long)
	 */
	public abstract SplitOutputStreamBuilder maxSize(long fileSizeBytes);


	/**
	 * <p>
	 * Split criteria for maximum number of records. Where the definition of a <i>record</i>
	 * is any {@code bytes} written between calls to {@link SplittableOutputStream#start()} and
	 * {@link SplittableOutputStream#finish()}
	 * </p>
	 *
	 * <p>
	 * May also be composed with maximum file size criteria.
	 * </p>
	 *
	 * @param numberOfRecords the maximum permitted number of records per file.
	 * @return the builder for further construction
	 * @throws IllegalArgumentException if {@code numberOfRecords < 0}
	 *
	 * @see #maxSize(long)
	 */
	public abstract SplitOutputStreamBuilder maxCount(long numberOfRecords);
}
