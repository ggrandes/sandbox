import java.net.Inet6Address;

/**
 * IPv6 usage in DNSBL / SURBL / RBL
 * <br/><a href="http://www.rfc-editor.org/rfc/rfc5782.txt">DNS Blacklists and Whitelists</a>
 * <br/><a href="http://www.rfc-editor.org/rfc/rfc3596.txt">DNS Extensions to Support IP Version 6</a>
 */
public class IPv6 {
	public static final char[] alpha = "0123456789abcdef".toCharArray();

	public static void main(final String[] args) throws Throwable {
		byte[] addr = Inet6Address.getByName("4321:0000:1:2:3:4:567:89ab").getAddress();
		StringBuilder sb = new StringBuilder(80);
		// Forward (expanded)
		sb.setLength(0);
		for (int i = 0; i < addr.length; i++) {
			final byte b = addr[i];
			sb.append(alpha[((b >> 4) & 0xF)]);
			sb.append(alpha[(b & 0xF)]);
			if ((i & 1) != 0)
				sb.append(':');
		}
		sb.setLength(sb.length()-1);
		System.out.println(sb.toString());
		//
		// Reverse (expanded)
		sb.setLength(0);
		for (int i = addr.length - 1; i >= 0; i--) {
			final byte b = addr[i];
			sb.append(alpha[(b & 0xF)]).append('.');
			sb.append(alpha[((b >> 4) & 0xF)]).append('.');
		}
		System.out.println(sb.append("ipv6.arpa.").toString());
	}
}
