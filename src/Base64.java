/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import java.nio.charset.Charset;

//import javax.xml.bind.DatatypeConverter;

/**
 * Simple Base64 encoder/decoder with support for URL Safe Encoding
 * 
 * @author Guillermo Grandes / guillermo.grandes[at]gmail.com
 */
public class Base64 {
	private static final int BITS_PER_B64_BYTE = 6;
	private static final int BYTES_PER_BLOCK_OF_8_BITS = 3;
	private static final int BYTES_PER_BLOCK_OF_6_BITS = 4;
	
	static final Charset charsetUTF8 = Charset.forName("UTF-8");
	static final Charset charsetISOLatin1 = Charset.forName("ISO-8859-1");

	/**
	 * Base64 Alphabet as specified in RFC-4648 page 6.
	 * 
	 * http://tools.ietf.org/html/rfc4648#page-6
	 */
	private static final byte[] ENCODE_TABLE_STD;
	private static final byte PADDING_CHAR_STD = '=';

	/**
	 * Base64 Alphabet as specified in RFC-4648 page 7 (URL-SAFE).
	 */
	private static final byte[] ENCODE_TABLE_URL_SAFE;

	/**
	 * Table for reverse Base64 encoding
	 */
	private static final byte[] DECODE_TABLE = new byte[256];

	static {
		final String BASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		final String EXTRA_STD = "+/";
		final String EXTRA_URL_SAFE = "-_";
		// Init encoding Tables
		ENCODE_TABLE_STD = (BASE + EXTRA_STD).getBytes();
		ENCODE_TABLE_URL_SAFE = (BASE + EXTRA_URL_SAFE).getBytes();
		// Init decoding Table
		for (int i = 0; i < BASE.length(); i++) {
			final char c = BASE.charAt(i);
			DECODE_TABLE[c] = (byte) i;
		}
		for (int i = 0; i < EXTRA_STD.length(); i++) {
			final char c = EXTRA_STD.charAt(i);
			DECODE_TABLE[c] = (byte) (i + BASE.length());
		}
		for (int i = 0; i < EXTRA_URL_SAFE.length(); i++) {
			final char c = EXTRA_URL_SAFE.charAt(i);
			DECODE_TABLE[c] = (byte) (i + BASE.length());
		}
	}

	private static final int getByte(final byte[] input, final int offset) {
		return ((offset < input.length) ? (input[offset] & 0xFF) : 0);
	}

	private static final void setByte(final byte[] output, final int offset, final int length, final byte b) {
		if (offset < length) {
			output[offset] = b;
		}
	}

	private static final int roundOut(final int len, final int round) {
		return (len / round) + ((len % round) > 0 ? 1 : 0);
	}

	private static final int paddingOut(final int len, final int pad) {
		return -((len % pad) - pad);
	}

	/**
	 * Encode to Base64
	 * 
	 * @param input
	 * @param urlSafeEncoding
	 * @return
	 */
	public static byte[] encode(final byte[] input, final boolean urlSafeEncoding) {
		final int lenInput = input.length;
		final int lenOutput = roundOut(lenInput * 8, BITS_PER_B64_BYTE);
		final int padding = (urlSafeEncoding ? 0 : paddingOut(lenOutput, BYTES_PER_BLOCK_OF_6_BITS));
		final byte[] output = new byte[lenOutput + padding];
		final byte[] transTable = (urlSafeEncoding ? ENCODE_TABLE_URL_SAFE : ENCODE_TABLE_STD);
		for (int i = 0, j = 0; i < lenInput; i += BYTES_PER_BLOCK_OF_8_BITS) {
			final int b1 = getByte(input, i);
			final int b2 = getByte(input, i + 1);
			final int b3 = getByte(input, i + 2);
			final long block = (b1 << 16 | b2 << 8 | b3) & 0x00FFFFFF;
			setByte(output, j++, lenOutput, transTable[(int) ((block >> 18) & 0x3F)]);
			setByte(output, j++, lenOutput, transTable[(int) ((block >> 12) & 0x3F)]);
			setByte(output, j++, lenOutput, transTable[(int) ((block >> 6) & 0x3F)]);
			setByte(output, j++, lenOutput, transTable[(int) (block & 0x3F)]);
		}
		if (padding > 0) {
			int j = output.length - padding;
			while (j < output.length) {
				output[j++] = (byte) PADDING_CHAR_STD;
			}
		}
		return output;
	}

	/**
	 * Encode to Base64
	 * 
	 * @param input
	 * @param urlSafeEncoding
	 * @return
	 */
	public static String encode(final String input, final boolean urlSafeEncoding) {
		return new String(encode(input.getBytes(charsetUTF8), urlSafeEncoding), charsetISOLatin1);
	}

	/**
	 * Decode from Base64
	 * 
	 * @param input
	 * @return
	 */
	public static byte[] decode(final byte[] input) {
		final int lenInput = findEnd(input);
		final int lenOutput = (lenInput * BITS_PER_B64_BYTE / 8);
		final byte[] output = new byte[lenOutput];
		final byte[] transTable = DECODE_TABLE;
		for (int i = 0, j = 0; i < lenInput; i += BYTES_PER_BLOCK_OF_6_BITS) {
			final int b1 = transTable[getByte(input, i)];
			final int b2 = transTable[getByte(input, i + 1)];
			final int b3 = transTable[getByte(input, i + 2)];
			final int b4 = transTable[getByte(input, i + 3)];
			final long block = ((b1 << 18) | (b2 << 12) | (b3 << 6) | b4) & 0x00FFFFFF;
			final byte x1 = (byte) ((block >> 16) & 0xFF);
			final byte x2 = (byte) ((block >> 8) & 0xFF);
			final byte x3 = (byte) (block & 0xFF);
			setByte(output, j++, lenOutput, x1);
			setByte(output, j++, lenOutput, x2);
			setByte(output, j++, lenOutput, x3);
		}
		return output;
	}

	/**
	 * Decode from Base64
	 * 
	 * @param input
	 * @return
	 */
	public static String decode(final String input) {
		return new String(decode(input.getBytes(charsetISOLatin1)), charsetUTF8);
	}

	private static final int findEnd(final byte[] input) {
		int j = input.length;
		while ((j > 0) && (input[--j] == PADDING_CHAR_STD))
			;
		return j + 1;
	}

	/**
	 * Simple Test
	 */
	public static void main(final String[] args) throws Throwable {
		final String strTest1 = "test";
		final String strTest2 = "hello world!!";
		//
		if (true) {
			System.out.println("----------- encode 1 base64 standard");
			String strInput = strTest1;
			System.out.println(strInput.length() + "\t" + strInput);
			String encoded = encode(strInput, false);
			System.out.println(encoded);
			//System.out.println(DatatypeConverter.printBase64Binary(strInput.getBytes(charsetUTF8)));
			String decoded = decode(encoded);
			System.out.println(decoded.length() + "\t" + decoded);
		}
		if (true) {
			System.out.println("----------- encode 1 base64 urlSafe");
			String strInput = strTest1;
			System.out.println(strInput.length() + "\t" + strInput);
			String encoded = encode(strInput, true);
			System.out.println(encoded);
			String decoded = decode(encoded);
			System.out.println(decoded.length() + "\t" + decoded);
		}
		//
		if (true) {
			System.out.println("----------- encode 2 base64 standard");
			String strInput = strTest2;
			System.out.println(strInput.length() + "\t" + strInput);
			String encoded = encode(strInput, false);
			System.out.println(encoded);
			//System.out.println(DatatypeConverter.printBase64Binary(strInput.getBytes(charsetUTF8)));
			String decoded = decode(encoded);
			System.out.println(decoded.length() + "\t" + decoded);
		}
		if (true) {
			System.out.println("----------- encode 2 base64 urlsafe");
			String strInput = strTest2;
			System.out.println(strInput.length() + "\t" + strInput);
			String encoded = encode(strInput, true);
			System.out.println(encoded);
			String decoded = decode(encoded);
			System.out.println(decoded.length() + "\t" + decoded);
		}
	}

}
