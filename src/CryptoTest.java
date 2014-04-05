import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Example of usage MessageDigest SHA-1 and Cipher AES-CBC (with IV)
 */
public class CryptoTest {
	public static void main(String[] args) throws Exception {
		String pwdText = "this is my password";
		String plainText = "hello how are you!";

		// Digest
		MessageDigest md = MessageDigest.getInstance("SHA1");
		md.update(pwdText.getBytes());
		byte byteData[] = md.digest();

		System.out.println("Digested value : " + bytesAsHex(byteData));
		byte keyData[] = new byte[128 >> 3]; // get only 128bits
		System.arraycopy(byteData, 0, keyData, 0, keyData.length);

		byte[] cipherText = null;
		SecureRandom rnd = new SecureRandom();
		IvParameterSpec iv = new IvParameterSpec(rnd.generateSeed(128 >> 3));
		// Crypt
		{
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyData, "AES"), iv);
			cipherText = cipher.doFinal(plainText.getBytes());
			System.out.println("crypted ciphertext (len: " + cipherText.length + ") is : "
					+ bytesAsHex(cipherText));
		}
		// Decrypt
		{
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyData, "AES"), iv);
			byte[] plainText2 = cipher.doFinal(cipherText);
			System.out.println("decrypted plaintext (len: " + plainText2.length + ") is : "
					+ new String(plainText2));
		}
	}

	//
	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	public static String bytesAsHex(final byte[] input) {
		final char[] sb = new char[input.length << 1];
		for (int j = 0; j < input.length; j++) {
			sb[(j << 1) | 0] = HEX_CHARS[((int) (input[j] >>> (1 << 2))) & 0xF];
			sb[(j << 1) | 1] = HEX_CHARS[((int) (input[j] >>> (0 << 2))) & 0xF];
		}
		return new String(sb);
	}
}