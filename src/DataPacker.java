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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.xml.bind.DatatypeConverter;

/**
 * Simple Data Packer
 * 
 * Sample usage (output):
 * 
 * <code><blockquote><pre>
 * Packer p = new Packer();
 * p.useCompress(false);
 * p.useCRC(false);
 * String s = "hello world";
 * String hs = "df0c290eae2b";
 * byte b = 42;
 * long l = 0x648C9A7109B4L;
 * int ni = -192813;
 * p.putString(s);
 * p.putHexString(hs);
 * p.putByte(b);
 * p.putVLong(l);
 * p.putVNegInt(ni);
 * p.flip();
 * String out = p.outputStringBase64URLSafe();
 * System.out.println(out.length() + "\t" + out);
 * </pre></blockquote></code>
 * 
 * Sample usage (load):
 * 
 * <code><blockquote><pre>
 * p = new Packer();
 * p.useCompress(false);
 * p.useCRC(false);
 * p.loadStringBase64URLSafe(input);
 * System.out.println(p.getString());
 * System.out.println(p.getHexStringUpper());
 * System.out.println(p.getByte());
 * System.out.println(p.getVLong());
 * System.out.println(p.getVNegInt());
 * </pre></blockquote></code>
 * 
 * @see java.nio.ByteBuffer
 * @author Guillermo Grandes / guillermo.grandes[at]gmail.com
 */
public class DataPacker {
	public static final int DEFAULT_SIZE = 4096;

	static final Charset charsetUTF8 = Charset.forName("UTF-8");
	static final Charset charsetISOLatin1 = Charset.forName("ISO-8859-1");
	static final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION,
			true);
	static final Inflater inflater = new Inflater(true);

	final ByteBuffer buf;
	boolean useCompress = false;
	boolean useCRC = false;

	/**
	 * Create Packer with default size of {@value #DEFAULT_SIZE}
	 * 
	 * @see java.nio.ByteBuffer
	 */
	public DataPacker() {
		this(DEFAULT_SIZE);
	}

	/**
	 * Create Packer with specified size
	 * 
	 * @param size
	 * @see java.nio.ByteBuffer
	 */
	public DataPacker(final int size) {
		this.buf = ByteBuffer.allocate(size);
	}

	/**
	 * Clear Packer Buffer see: {@link java.nio.ByteBuffer#clear()}
	 * 
	 * @return
	 */
	public DataPacker clear() {
		buf.clear();
		return this;
	}

	/**
	 * Flip Packer Buffer see: {@link java.nio.ByteBuffer#flip()}
	 * 
	 * @return
	 */
	public DataPacker flip() {
		buf.flip();
		return this;
	}

	/**
	 * Rewind Packer Buffer see: {@link java.nio.ByteBuffer#rewind()}
	 * 
	 * @return
	 */
	public DataPacker rewind() {
		buf.rewind();
		return this;
	}

	/**
	 * Sets the usage of Deflater/Inflater
	 * 
	 * @param useCompress
	 * @return
	 */
	public DataPacker useCompress(final boolean useCompress) {
		this.useCompress = useCompress;
		return this;
	}

	/**
	 * Sets the usage of CRC for sanity
	 * 
	 * @param useCRC
	 * @return
	 */
	public DataPacker useCRC(final boolean useCRC) {
		this.useCRC = useCRC;
		return this;
	}

	/**
	 * Return the underling ByteBuffer
	 * 
	 * @return
	 */
	public ByteBuffer getByteBuffer() {
		return buf;
	}

	// ------------- PUT -------------

	/**
	 * Put native byte (fixed length)
	 * 
	 * @param value
	 * @return
	 * @see #getByte()
	 */
	public DataPacker putByte(final byte value) {
		buf.put(value);
		return this;
	}

	/**
	 * Put native char (fixed length)
	 * 
	 * @param value
	 * @return
	 * @see #getChar()
	 */
	public DataPacker putChar(final char value) {
		buf.putChar(value);
		return this;
	}

	/**
	 * Put native short (fixed length)
	 * 
	 * @param value
	 * @return
	 * @see #getShort()
	 */
	public DataPacker putShort(final short value) {
		buf.putShort(value);
		return this;
	}

	/**
	 * Put native double (fixed length)
	 * 
	 * @param value
	 * @return
	 * @see #getDouble()
	 */
	public DataPacker putDouble(final double value) {
		buf.putDouble(value);
		return this;
	}

	/**
	 * Put native float (fixed length)
	 * 
	 * @param value
	 * @return
	 * @see #getFloat()
	 */
	public DataPacker putFloat(final float value) {
		buf.putFloat(value);
		return this;
	}

	/**
	 * Put native int (fixed length)
	 * 
	 * @param value
	 * @return
	 * @see #getInt()
	 */
	public DataPacker putInt(final int value) {
		buf.putInt(value);
		return this;
	}

	/**
	 * Put native long (fixed length)
	 * 
	 * @param value
	 * @return
	 * @see #getLong()
	 */
	public DataPacker putLong(final long value) {
		buf.putLong(value);
		return this;
	}

	/**
	 * Put native int in variable length format (support negative value, but
	 * size is longer)
	 * 
	 * @param value
	 * @return
	 * @see #getVInt()
	 */
	public DataPacker putVInt(final int value) {
		encodeVInt(buf, value);
		return this;
	}

	/**
	 * Put native negative int in variable length format (support positive
	 * value, but size is longer)
	 * 
	 * @param value
	 * @return
	 * @see #getVNegInt()
	 */
	public DataPacker putVNegInt(final int value) {
		encodeVInt(buf, -value);
		return this;
	}

	/**
	 * Put native long in variable length format (support negative value, but
	 * size is longer)
	 * 
	 * @param value
	 * @return
	 * @see #getVLong()
	 */
	public DataPacker putVLong(final long value) {
		encodeVLong(buf, value);
		return this;
	}

	/**
	 * Put native negative long in variable length format (support positive
	 * value, but size is longer)
	 * 
	 * @param value
	 * @return
	 * @see #getVNegLong()
	 */
	public DataPacker putVNegLong(final long value) {
		encodeVLong(buf, -value);
		return this;
	}

	/**
	 * Put Byte array
	 * 
	 * @param value
	 * @return
	 * @see #getBytes()
	 */
	public DataPacker putBytes(final byte[] value) {
		encodeVInt(buf, value.length);
		buf.put(value);
		return this;
	}

	/**
	 * Put String in UTF-8 format
	 * 
	 * @param value
	 * @return
	 * @see #getString()
	 */
	public DataPacker putString(final String value) {
		encodeString(buf, value);
		return this;
	}

	/**
	 * Put Hex String ("0123456789ABCDEF")
	 * 
	 * @param value
	 * @return
	 * @see #getHexStringLower()
	 * @see #getHexStringUpper()
	 */
	public DataPacker putHexString(final String value) {
		try {
			byte[] hex = fromHex(value);
			encodeVInt(buf, hex.length);
			buf.put(hex);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid input string", e);
		}
		return this;
	}

	// ------------- OUTPUT -------------

	/**
	 * Output Base64 string
	 * <p>
	 * Base64 info: <a href="http://en.wikipedia.org/wiki/Base64">Base64</a>
	 * 
	 * @see #loadStringBase64(String)
	 */
	public String outputStringBase64() {
		return DatatypeConverter.printBase64Binary(outputBytes());
	}

	/**
	 * Output Base64 string replacing "+/" to "-_" that is URL safe and removing
	 * base64 padding "="
	 * <p>
	 * RFC-4648 info, The "URL and Filename safe" Base 64 Alphabet: <a
	 * href="http://tools.ietf.org/html/rfc4648#page-7">RFC-4648</a>
	 * 
	 * @see #loadStringBase64URLSafe(String)
	 */
	public String outputStringBase64URLSafe() {
		char[] tmpBuf = outputStringBase64().toCharArray();
		int len = 0;
		END: for (int i = 0; i < tmpBuf.length; i++) {
			final char c = tmpBuf[i];
			switch (c) {
			case '+':
				tmpBuf[i] = '-';
				break;
			case '/':
				tmpBuf[i] = '_';
				break;
			case '=':
				break END;
			}
			len++;
		}
		return new String(tmpBuf, 0, len);
	}

	/**
	 * Output string in hex format
	 * 
	 * @return
	 * @see #loadStringHex(String)
	 */
	public String outputStringHex() {
		return toHex(outputBytes(), true);
	}

	/**
	 * Output string in raw format (ISO-8859-1)
	 * 
	 * @return
	 * @see #loadStringRAW(String)
	 */
	public String outputStringRAW() {
		return new String(outputBytes(), charsetISOLatin1);
	}

	/**
	 * Output bytes in raw format
	 * 
	 * @return
	 * @see #loadBytes(byte[])
	 */
	public byte[] outputBytes() {
		byte[] tmpBuf = buf.array();
		int len = buf.limit();
		if (useCompress) {
			tmpBuf = deflate(tmpBuf, len);
			len = tmpBuf.length;
		}
		if (useCRC) {
			tmpBuf = resizeBuffer(tmpBuf, len + 1);
			tmpBuf[len] = (byte) crc8(tmpBuf, 0, len);
			len = tmpBuf.length;
		}
		return resizeBuffer(tmpBuf, len);
	}

	// ------------- GET -------------

	/**
	 * Get native byte (fixed length)
	 * 
	 * @return
	 * @see #putByte(byte)
	 */
	public byte getByte() {
		return buf.get();
	}

	/**
	 * Get native char (fixed length)
	 * 
	 * @return
	 * @see #putChar(char)
	 */
	public char getChar() {
		return buf.getChar();
	}

	/**
	 * Get native short (fixed length)
	 * 
	 * @return
	 * @see #putShort(short)
	 */
	public short getShort() {
		return buf.getShort();
	}

	/**
	 * Get native double (fixed length)
	 * 
	 * @return
	 * @see #putDouble(double)
	 */
	public double getDouble() {
		return buf.getDouble();
	}

	/**
	 * Get native float (fixed length)
	 * 
	 * @return
	 * @see #putFloat(float)
	 */
	public float getFloat() {
		return buf.getFloat();
	}

	/**
	 * Get native int (fixed length)
	 * 
	 * @return
	 * @see #putInt(int)
	 */
	public int getInt() {
		return buf.getInt();
	}

	/**
	 * Get native long (fixed length)
	 * 
	 * @return
	 * @see #putLong(long)
	 */
	public long getLong() {
		return buf.getLong();
	}

	/**
	 * Get native int stored in variable length format (support positive value,
	 * but size is longer)
	 * 
	 * @return
	 * @see #getVNegInt()
	 */
	public int getVInt() {
		return decodeVInt(buf);
	}

	/**
	 * Get native negative int stored in variable length format (support
	 * positive value, but size is longer)
	 * 
	 * @return
	 * @see #getVInt()
	 */
	public int getVNegInt() {
		return -decodeVInt(buf);
	}

	/**
	 * Get native long stored in variable length format (support positive value,
	 * but size is longer)
	 * 
	 * @return
	 * @see #getVNegLong()
	 */
	public long getVLong() {
		return decodeVLong(buf);
	}

	/**
	 * Get native negative long stored in variable length format (support
	 * positive value, but size is longer)
	 * 
	 * @return
	 * @see #getVLong()
	 */
	public long getVNegLong() {
		return -decodeVLong(buf);
	}

	/**
	 * Get Byte array
	 * 
	 * @return
	 * @see #putBytes(byte[])
	 */
	public byte[] getBytes() {
		int len = decodeVInt(buf);
		byte[] bytes = new byte[len];
		buf.get(bytes);
		return bytes;
	}

	/**
	 * Get String stored in UTF-8 format
	 * 
	 * @return
	 * @see #putString(String)
	 */
	public String getString() {
		return decodeString(buf);
	}

	/**
	 * Get Hex String in upper case ("0123456789ABCDEF")
	 * 
	 * @return
	 * @see #putHexString(String)
	 * @see #getHexStringLower()
	 */
	public String getHexStringUpper() {
		int len = decodeVInt(buf);
		byte[] hex = new byte[len];
		buf.get(hex);
		return toHex(hex, true);
	}

	/**
	 * Get Hex String in lower case ("0123456789abcdef")
	 * 
	 * @return
	 * @see #putHexString(String)
	 * @see #getHexStringUpper()
	 */
	public String getHexStringLower() {
		int len = decodeVInt(buf);
		byte[] hex = new byte[len];
		buf.get(hex);
		return toHex(hex, false);
	}

	// ------------- LOAD -------------

	/**
	 * Load Base64 string
	 * <p>
	 * Base64 info: <a href="http://en.wikipedia.org/wiki/Base64">Base64</a>
	 * 
	 * @see #outputStringBase64()
	 */
	public DataPacker loadStringBase64(final String in) {
		final byte[] tmpBuf = DatatypeConverter.parseBase64Binary(in);
		return loadBytes(tmpBuf);
	}

	/**
	 * Load URL safe Base64 string
	 * <p>
	 * RFC-4648 info, The "URL and Filename safe" Base 64 Alphabet: <a
	 * href="http://tools.ietf.org/html/rfc4648#page-7">RFC-4648</a>
	 * 
	 * @see DataPacker#outputStringBase64URLSafe()
	 */
	public DataPacker loadStringBase64URLSafe(final String in) {
		final int inlen = in.length();
		final int pad = (((inlen % 4) > 0) ? (4 - (inlen % 4)) : 0);
		final int outlen = inlen + pad;
		final char[] charBuf = new char[outlen];
		Arrays.fill(charBuf, inlen, charBuf.length, '=');
		for (int i = 0; i < inlen; i++) {
			final char c = in.charAt(i);
			switch (c) {
			case '-':
				charBuf[i] = '+';
				break;
			case '_':
				charBuf[i] = '/';
				break;
			default:
				charBuf[i] = c;
				break;
			}
		}
		final String tmpStr = new String(charBuf, 0, outlen);
		return loadStringBase64(tmpStr);
	}

	/**
	 * Load string in hex format
	 * 
	 * @return
	 * @throws ParseException
	 * @see #outputStringHex()
	 */
	public DataPacker loadStringHex(final String in) {
		try {
			byte[] tmpBuf = fromHex(in);
			return loadBytes(tmpBuf);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid input string", e);
		}
	}

	/**
	 * Load string in raw format (ISO-8859-1)
	 * 
	 * @return
	 * @see #outputStringRAW()
	 */
	public DataPacker loadStringRAW(final String in) {
		final byte[] tmpBuf = in.getBytes(charsetISOLatin1);
		return loadBytes(tmpBuf);
	}

	/**
	 * Load bytes[] in raw format (ISO-8859-1)
	 * 
	 * @return
	 * @see #outputBytes()
	 */
	public DataPacker loadBytes(final byte[] in) {
		if (useCRC) {
			int crc = crc8(in, 0, in.length - 1);
			boolean crcOK = (crc == in[in.length - 1]);
			if (!crcOK) {
				throw new IllegalArgumentException("Invalid CRC");
			}
		}
		buf.clear();
		buf.put(useCompress ? inflate(in) : in);
		buf.flip();
		buf.rewind();
		return this;
	}

	// ------------- INTERNAL -------------

	/**
	 * Calculate CRC-8 of input
	 * <p>
	 * <a href="http://en.wikipedia.org/wiki/Cyclic_redundancy_check">CRC-8</a>
	 * 
	 * @param input
	 * @param offset
	 * @param len
	 * @return
	 */
	static final int crc8(final byte[] input, final int offset, final int len) {
		final int poly = 0x0D5;
		int crc = 0;
		for (int i = 0; i < len; i++) {
			final byte c = input[offset + i];
			crc ^= c;
			for (int j = 0; j < 8; j++) {
				if ((crc & 0x80) != 0) {
					crc = ((crc << 1) ^ poly);
				} else {
					crc <<= 1;
				}
			}
			crc &= 0xFF;
		}
		return crc;
	}

	/**
	 * Resize input buffer to newsize
	 * 
	 * @param buf
	 * @param newsize
	 * @return
	 */
	static final byte[] resizeBuffer(final byte[] buf, final int newsize) {
		if (buf.length == newsize)
			return buf;
		final byte[] newbuf = new byte[newsize];
		System.arraycopy(buf, 0, newbuf, 0, Math.min(buf.length, newbuf.length));
		return newbuf;
	}

	/**
	 * Transform byte array to Hex String
	 * 
	 * @param input
	 * @return
	 */
	static final String toHex(final byte[] input, final int len,
			final boolean upper) {
		final char[] hex = new char[len << 1];
		for (int i = 0, j = 0; i < len; i++) {
			final int bx = input[i];
			final int bh = ((bx >> 4) & 0xF);
			final int bl = (bx & 0xF);
			if ((bh >= 0) && (bh <= 9)) {
				hex[j++] |= (bh + '0');
			} else if ((bh >= 0xA) && (bh <= 0xF)) {
				hex[j++] |= (bh - 0xA + (upper ? 'A' : 'a'));
			}
			if ((bl >= 0x0) && (bl <= 0x9)) {
				hex[j++] |= (bl + '0');
			} else if ((bl >= 0xA) && (bl <= 0xF)) {
				hex[j++] |= (bl - 0xA + (upper ? 'A' : 'a'));
			}
		}
		return new String(hex);
	}

	/**
	 * Transform byte array to Hex String
	 * 
	 * @param input
	 * @param upper
	 * @return
	 */
	static final String toHex(final byte[] input, final boolean upper) {
		return toHex(input, input.length, upper);
	}

	/**
	 * Transform Hex String to byte array
	 * 
	 * @param hex
	 * @return
	 * @throws ParseException
	 */
	static final byte[] fromHex(final String hex) throws ParseException {
		final int len = hex.length();
		final byte[] out = new byte[len / 2];
		for (int i = 0, j = 0; i < len; i++) {
			char c = hex.charAt(i);
			int v = 0;
			if ((c >= '0') && (c <= '9')) {
				v = (c - '0');
			} else if ((c >= 'A') && (c <= 'F')) {
				v = (c - 'A') + 0xA;
			} else if ((c >= 'a') && (c <= 'f')) {
				v = (c - 'a') + 0xA;
			} else {
				throw new ParseException("Invalid char", j);
			}
			if ((i & 1) == 0) {
				out[j] |= (v << 4);
			} else {
				out[j++] |= v;
			}
		}
		return out;
	}

	/**
	 * Write String into buffer in UTF-8 format
	 * 
	 * @param buf
	 * @param value
	 */
	static final void encodeString(final ByteBuffer out, final String value) {
		final byte[] utf = value.getBytes(charsetUTF8);
		encodeVInt(out, utf.length);
		out.put(utf);
	}

	/**
	 * Read String from buffer stored in UTF-8 format
	 * 
	 * @param buf
	 * @return
	 */
	static final String decodeString(final ByteBuffer in) {
		int len = decodeVInt(in);
		byte[] utf = new byte[len];
		in.get(utf);
		return new String(utf, 0, len, charsetUTF8);
	}

	/**
	 * Write native int into buffer in variable length format
	 * 
	 * @param out
	 * @param value
	 */
	static final void encodeVInt(final ByteBuffer out, int value) {
		int i = 0;
		while (((value & ~0x7FL) != 0L) && ((i += 7) < 32)) {
			out.put((byte) ((value & 0x7FL) | 0x80L));
			value >>>= 7;
		}
		out.put((byte) value);
	}

	/**
	 * Write native long into buffer in variable length format
	 * 
	 * @param out
	 * @param value
	 */
	static final void encodeVLong(final ByteBuffer out, long value) {
		// org.apache.lucene.util.packed.AbstractBlockPackedWriter
		int i = 0;
		while (((value & ~0x7FL) != 0L) && ((i += 7) < 64)) {
			out.put((byte) ((value & 0x7FL) | 0x80L));
			value >>>= 7;
		}
		out.put((byte) value);
	}

	/**
	 * Read native int from buffer in variable length format
	 * 
	 * @param in
	 */
	static final int decodeVInt(final ByteBuffer in) {
		int value = 0;
		for (int i = 0; i <= 32; i += 7) {
			final byte b = in.get();
			value |= ((b & 0x7FL) << i);
			if (b >= 0)
				return value;
		}
		return value;
	}

	/**
	 * Read native long from buffer in variable length format
	 * 
	 * @param in
	 */
	static final long decodeVLong(final ByteBuffer in) {
		// org.apache.lucene.util.packed.BlockPackedReaderIterator
		long value = 0;
		for (int i = 0; i <= 64; i += 7) {
			final byte b = in.get();
			value |= ((b & 0x7FL) << i);
			if (b >= 0)
				return value;
		}
		return value;
	}

	/**
	 * Deflate input buffer
	 * 
	 * @param in
	 * @param len
	 * @return
	 */
	static final byte[] deflate(final byte[] in, final int len) {
		byte[] defBuf = new byte[len << 1];
		int payloadLength;
		synchronized (deflater) {
			deflater.reset();
			deflater.setInput(in, 0, len);
			deflater.finish();
			payloadLength = deflater.deflate(defBuf);
		}
		return resizeBuffer(defBuf, payloadLength);
	}

	/**
	 * Inflate input buffer
	 * 
	 * @param in
	 * @return
	 */
	static final byte[] inflate(final byte[] in) {
		try {
			byte[] infBuf = new byte[in.length << 1];
			int payloadLength;
			synchronized (inflater) {
				inflater.reset();
				inflater.setInput(in);
				payloadLength = inflater.inflate(infBuf);
			}
			return resizeBuffer(infBuf, payloadLength);
		} catch (DataFormatException e) {
			throw new IllegalArgumentException("Compressed data", e);
		}
	}
}
