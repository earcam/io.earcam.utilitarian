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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;

import org.junit.jupiter.api.Test;

public class SimpleTokenizerTest {

	@Test
	public void simple() throws Exception
	{
		String input = "The  quick,brown fox\r\n\r\njumps -- over\t\t\tthe lazy dog!";

		SimpleTokenizer tokenizer = new SimpleTokenizer();

		List<String> tokens = tokenizer.tokenize(input);

		assertThat(tokens, contains(
				"the", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog"));
	}
}
