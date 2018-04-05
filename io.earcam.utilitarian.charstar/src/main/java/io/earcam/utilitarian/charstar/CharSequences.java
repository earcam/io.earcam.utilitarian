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
package io.earcam.utilitarian.charstar;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class CharSequences {

	private CharSequences()
	{

	}


	public static boolean startsWith(CharSequence text, CharSequence prefix)
	{
		if(prefix.length() > text.length()) {
			return false;
		}
		for(int i = 0; i < prefix.length(); i++) {
			if(text.charAt(i) != prefix.charAt(i)) {
				return false;
			}
		}
		return true;
	}


	public static boolean endsWith(CharSequence text, CharSequence suffix)
	{
		if(suffix.length() > text.length()) {
			return false;
		}
		for(int i = text.length() - 1, j = suffix.length() - 1; j >= 0; i--, j--) {
			if(text.charAt(i) != suffix.charAt(j)) {
				return false;
			}
		}
		return true;
	}


	public static boolean same(@Nullable CharSequence a, @Nullable CharSequence b)
	{
		if(a == null || b == null) {
			return a == b;
		}
		if(a.length() != b.length()) {
			return false;
		}
		for(int i = 0; i < a.length(); i++) {
			if(a.charAt(i) != b.charAt(i)) {
				return false;
			}
		}
		return true;
	}


	public static int hashCode(CharSequence sequence)
	{
		int hash = 0;
		for(int i = 0; i < sequence.length(); i++) {
			hash = 31 * hash + sequence.charAt(i);
		}
		return hash;
	}


	public static CharSequence trim(CharSequence padded)
	{
		int s = 0;
		int e = padded.length();
		while(Character.isWhitespace(padded.charAt(s))) {
			s += 1;
		}
		while(Character.isWhitespace(padded.charAt(e - 1))) {
			e -= 1;
		}
		return padded.subSequence(s, e);
	}


	public static @Nullable CharSequence replace(@Nullable CharSequence sequence, char find, char replace)
	{
		if(sequence == null) {
			return null;
		}
		char[] chars = new char[sequence.length()];
		for(int i = 0; i < chars.length; i++) {
			chars[i] = sequence.charAt(i);
			if(chars[i] == find) {
				chars[i] = replace;
			}
		}
		return CharStar.charSequence(chars);
	}


	public static boolean isEmpty(@Nullable CharSequence text)
	{
		return text == null || text.length() == 0;
	}


	public static int lastIndexOf(CharSequence text, char character)
	{
		for(int i = text.length() - 1; i >= 0; i--) {
			if(text.charAt(i) == character) {
				return i;
			}
		}
		return -1;
	}


	public static int indexOf(CharSequence text, char character)
	{
		for(int i = 0; i < text.length(); i++) {
			if(text.charAt(i) == character) {
				return i;
			}
		}
		return -1;
	}


	public static byte[] toBytes(CharSequence utf8Sequence)
	{
		return toBytes(utf8Sequence, UTF_8);
	}


	public static byte[] toBytes(CharSequence sequence, Charset charset)
	{
		ByteBuffer buffer = charset.encode(CharBuffer.wrap(sequence));
		byte[] bytes = new byte[buffer.remaining()];  // TODO avoid GC?
		buffer.get(bytes);
		return bytes;
	}
}
