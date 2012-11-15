import java.util.Formatter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ExampleHMAC {
	private static String toHex(final byte[] bytes) {
		final StringBuilder sb = new StringBuilder(bytes.length << 1);
		final Formatter formatter = new Formatter(sb);
		for (final byte b : bytes) {
			formatter.format("%02x", b);
		}
		return sb.toString();
	}

	public static String doHash(final String str, final String passphrase)
			throws NoSuchAlgorithmException, InvalidKeyException,
			UnsupportedEncodingException {
		final Mac hmac = Mac.getInstance("HmacSHA256");
		hmac.init(new SecretKeySpec(passphrase.getBytes("UTF-8"), "HmacSHA256"));
		return toHex(hmac.doFinal(str.getBytes("UTF-8")));
	}

	public static void main(String[] args) throws Exception {
		System.out.println("hash: " + doHash("Loren ipsum", "changeit"));
	}
}
