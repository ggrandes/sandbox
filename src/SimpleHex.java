// Fast int/long/byte[] to Hex String (left-zero-padding)
public class SimpleHex {
	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	public static String intAsHex(final int input) {
		final char[] sb = new char[8];
		final int len = (sb.length-1);
		for (int i = 0; i <= len; i++) { // MSB
			sb[i] = HEX_CHARS[((int)(input >>> ((len - i)<<2))) & 0xF];
		}
		return new String(sb);
	}
	public static String longAsHex(final long input) {
		final char[] sb = new char[16];
		final int len = (sb.length-1);
		for (int i = 0; i <= len; i++) { // MSB
			sb[i] = HEX_CHARS[((int)(input >>> ((len - i)<<2))) & 0xF];
		}
		return new String(sb);
	}
	public static String bytesAsHex(final byte[] input) {
		final char[] sb = new char[input.length<<1];
		for (int j = 0; j < input.length; j++) {
			sb[(j<<1)|0] = HEX_CHARS[((int)(input[j] >>> (1<<2))) & 0xF];
			sb[(j<<1)|1] = HEX_CHARS[((int)(input[j] >>> (0<<2))) & 0xF];
		}
		return new String(sb);
	}
}
