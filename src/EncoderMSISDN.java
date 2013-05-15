/**
 * Encoding of MSISDN numbers
 * <p>
 * <a href="https://en.wikipedia.org/wiki/MSISDN">MSISDN</a>
 */
public class EncoderMSISDN {

	/**
	 * Encode MSISDN in a Long
	 * @param msisdn
	 * @return
	 */
	public static long toLong(final String msisdn) {
		final int len = msisdn.length();
		long value = 0;
		int begin = 0;
		if (msisdn.charAt(begin) == '+') {
			value = 1;
			begin++;
		} else {
			value = 2;
		}
		for (int i = begin; i < len; i++) {
			final char c = msisdn.charAt(i);
			if (c >= '0' && c <= '9') {
				value = (value * 10) + (c - '0');
			} else {
				return -1;
			}
		}
		return value;
	}

	/**
	 * Decode MSISDN from a Long
	 * @param msisdn
	 * @return
	 */
	public static String fromLong(long msisdn) {
		final int maxlen = 19;
		char[] buf = new char[maxlen];
		int i = buf.length - 1;
		while (msisdn > 0) {
			buf[i] = (char) ('0' + (msisdn % 10));
			msisdn /= 10;
			i--;
		}
		if (buf[i + 1] == '1') {
			buf[i + 1] = '+';
		} else if (buf[i + 1] == '2') {
			i++;
		}
		return new String(buf, i + 1, buf.length - 1 - i);
	}

	public static void main(String[] args) {
		final String phone1 = "677123456";
		final String phone2 = "+34655123456";
		final String phone3 = "+0035688123456";
		final long l1 = toLong(phone1);
		final long l2 = toLong(phone2);
		final long l3 = toLong(phone3);
		System.out.println(Long.MAX_VALUE + " --- LONG");
		System.out.println(Integer.MAX_VALUE + " --- Integer");
		System.out.println(l1);
		System.out.println(l2);
		System.out.println(l3);
		System.out.println(fromLong(l1));
		System.out.println(fromLong(l2));
		System.out.println(fromLong(l3));
	}

}
