import java.text.ParseException;

// Fast int/long/byte[] to Hex String (left-zero-padding)
public class SimpleHex {
	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	/**
	 * Transform int to Hex String
	 * 
	 * @param input
	 * @return
	 */
	public static String intAsHex(final int input) {
		final char[] sb = new char[8];
		final int len = (sb.length - 1);
		for (int i = 0; i <= len; i++) { // MSB
			sb[i] = HEX_CHARS[((int) (input >>> ((len - i) << 2))) & 0xF];
		}
		return new String(sb);
	}

	/**
	 * Transform long to Hex String
	 * 
	 * @param input
	 * @return
	 */
	public static String longAsHex(final long input) {
		final char[] sb = new char[16];
		final int len = (sb.length - 1);
		for (int i = 0; i <= len; i++) { // MSB
			sb[i] = HEX_CHARS[((int) (input >>> ((len - i) << 2))) & 0xF];
		}
		return new String(sb);
	}

	/**
	 * Transform byte array to Hex String
	 * 
	 * @param input
	 * @return
	 */
	public static String bytesAsHex(final byte[] input) {
		final char[] sb = new char[input.length << 1];
		for (int j = 0; j < input.length; j++) {
			sb[(j << 1) | 0] = HEX_CHARS[((int) (input[j] >>> (1 << 2))) & 0xF];
			sb[(j << 1) | 1] = HEX_CHARS[((int) (input[j] >>> (0 << 2))) & 0xF];
		}
		return new String(sb);
	}

	/**
	 * Transform byte array to Hex String
	 * 
	 * @param input
	 * @param offset
	 * @param len
	 * @param upper
	 * @return
	 */
	public static final String toHex(final byte[] input, final int offset,
			final int len, final boolean upper) {
		final char[] hex = new char[len << 1];
		for (int i = 0, j = 0; i < len; i++) {
			final int bx = input[offset + i];
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
	 * Transform Hex String to byte array
	 * 
	 * @param hex
	 * @return
	 * @throws ParseException
	 */
	public static final byte[] fromHex(final String hex) throws ParseException {
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

	public static void main(String[] args) {
		String test = "hello world";
		System.out.println(toHex(test.getBytes(), 0, test.length(), true));
		System.out.println(toHex(test.getBytes(), 6, 2, true));
		System.out.println(bytesAsHex(test.getBytes()));
	}
}
