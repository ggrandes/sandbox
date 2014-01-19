import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A mutable sequence of bytes. This class provides an API similar to StringBuilder but for bytes.
 * 
 * The principal operations on a ByteArrayBuilder are the append methods, which are overloaded so as
 * to accept data of any type. The append methods always adds these characters at the
 * end of the builder.
 * 
 * Every byte array builder has a capacity. As long as the length of the byte
 * sequence contained in the builder does not exceed the capacity, it is not necessary to allocate a
 * new internal buffer. If the internal buffer overflows, it automatically double their size in power of 2.
 * 
 * Instances of ByteArrayBuilder are not safe for use by multiple threads.
 */
public final class ByteArrayBuilder {
	public static final int INITIAL_SIZE = 16;

	private byte[] buf;
	private int length;

	/**
	 * Constructs a byte array builder with no bytes in it and an initial capacity of 16 characters.
	 */
	public ByteArrayBuilder() {
		this(INITIAL_SIZE);
	}

	/**
	 * Constructs a string builder with no bytes in it and an initial capacity specified by the capacity
	 * argument.
	 * 
	 * @param capacity
	 */
	public ByteArrayBuilder(final int capacity) {
		this.buf = new byte[capacity];
	}

	/**
	 * Appends the byte (converted from int) to the sequence.
	 * 
	 * @param b
	 * @return
	 */
	public ByteArrayBuilder append(final int b) {
		return append((byte) (b & 0xFF));
	}

	/**
	 * Appends the byte to the sequence.
	 * 
	 * @param b
	 * @return
	 */
	public ByteArrayBuilder append(final byte b) {
		if ((length + 1) > buf.length) {
			resize(findNextPowerOfTwo(length + 1));
		}
		buf[length++] = b;
		return this;
	}

	/**
	 * Appends the bytes to the sequence.
	 * 
	 * @param data
	 * @return
	 */
	public ByteArrayBuilder append(final byte[] data) {
		append(data, 0, data.length);
		return this;
	}

	/**
	 * Appends the bytes to the sequence.
	 * 
	 * @param bb
	 * @param offset
	 * @param len
	 * @return
	 */
	public ByteArrayBuilder append(final byte[] bb, final int offset, final int len) {
		if ((length + len) > buf.length) {
			resize(findNextPowerOfTwo(length + len));
		}
		System.arraycopy(bb, offset, buf, length, len);
		length += len;
		return this;
	}

	/**
	 * Returns the byte value at the specified index. An index ranges from zero to
	 * length() - 1. The first byte value of the sequence is at index zero, the next at index one, and so on,
	 * as for array indexing.
	 * 
	 * @param index
	 * @return
	 */
	public byte byteAt(final int index) {
		return buf[index];
	}

	/**
	 * Returns the char value at the specified index (converted from byte). An index ranges from zero to
	 * length() - 1. The first char value of the sequence is at index zero, the next at index one, and so on,
	 * as for array indexing.
	 * 
	 * @param index
	 * @return
	 */
	public char charAt(final int index) {
		return (char) (buf[index] & 0xFF);
	}

	/**
	 * Return reference to internal byte array
	 */
	public byte[] getInternalBuffer() {
		return buf;
	}

	/**
	 * Return a new allocated byte array
	 */
	public byte[] toByteArray() {
		final byte[] newbuf = new byte[length];
		System.arraycopy(buf, 0, newbuf, 0, length);
		return newbuf;
	}

	/**
	 * Return a new allocated char array
	 */
	public char[] toCharArray() {
		final char[] newbuf = new char[length];
		for (int i = 0; i < length; i++) {
			newbuf[i] = (char) (buf[i] & 0xFF);
		}
		return newbuf;
	}

	/**
	 * Constructs a new String by decoding the array of bytes using the platform's default
	 * charset. The length of the new String is a function of the charset, and hence may not be equal to the
	 * length of the subarray.
	 * 
	 * @see String#String(byte[], int, int)
	 */
	public String toString() {
		return new String(buf, 0, length);
	}

	/**
	 * Constructs a new String by decoding the array of bytes using the specified charset. The
	 * length of the new String is a function of the charset, and hence may not be equal to the length of the
	 * subarray.
	 * 
	 * @param charset
	 * @return
	 * @see String#String(byte[], int, int, Charset)
	 */
	public String toString(final Charset charset) {
		return new String(buf, 0, length, charset);
	}

	/**
	 * Constructs a new String by decoding the array of bytes using the specified charset. The
	 * length of the new String is a function of the charset, and hence may not be equal to the length of the
	 * subarray.
	 * 
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 * @see String#String(byte[], int, int, String)
	 */
	public String toString(final String charset) throws UnsupportedEncodingException {
		return new String(buf, 0, length, charset);
	}

	/**
	 * Returns the current capacity.
	 */
	public int capacity() {
		return buf.length;
	}

	/**
	 * Returns the length (character count).
	 */
	public int length() {
		return length;
	}

	/**
	 * Sets the length of the character sequence.
	 * 
	 * @param newLength
	 */
	public void setLength(final int newLength) {
		if (newLength < 0)
			throw new StringIndexOutOfBoundsException(newLength);
		if (newLength > buf.length) {
			resize(newLength);
		} else if (length < newLength) {
			Arrays.fill(buf, length, newLength, (byte) 0);
		}
		length = newLength;
	}

	/**
	 * Attempts to reduce storage used for the byte array.
	 */
	public void trimToSize() {
		if (buf.length > length)
			resize(length);
	}

	private static final int findNextPowerOfTwo(final int n) {
		return (int) Math.pow(2, Math.floor(Math.log(n) / Math.log(2)) + 1);
	}

	private final void resize(final int newSize) {
		final byte[] newbuf = new byte[newSize];
		System.arraycopy(buf, 0, newbuf, 0, Math.min(length, buf.length));
		this.buf = newbuf;
	}

	public static void main(String[] args) {
		final ByteArrayBuilder bab = new ByteArrayBuilder(1);
		bab.append('t').append('e').append('s').append('t');
		System.out.println(bab.capacity() + " " + bab.length() + " " + bab.toString());
		bab.append('X');
		bab.setLength(1);
		bab.setLength(4);
		System.out.println(bab.capacity() + " " + bab.length() + " " + bab.toString());
	}
}
