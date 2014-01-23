import java.security.SecureRandom;

/**
 * Normas para los Passwords:
 * <ul>
 * <li>Al menos 8 characters</li>
 * <li>Al menos 1 caracter de cada grupo:</li>
 * <ul>
 * <li>Letras mayusculas <b>[A-Z]</b></li>
 * <li>Letras minusculas <b>[a-z]</b></li>
 * <li>Numeros <b>[0-9]</b></li>
 * <li>Caracteres especiales: <b><[{(#$%&*?!:.,=+-_~^)}]></b></li>
 * <ul>
 * <li>No espacios</li>
 * <li>No letras acentuadas</li>
 * <li>No partes del username o ID</li>
 * </ul>
 * </ul>
 * 
 * <a href="http://en.wikipedia.org/wiki/Password_strength">Password strength</a>
 * <a href="http://howsecureismypassword.net/">How Secure is My Password?</a>
 */
public class PasswordGenerator {
	// Excluded visual similar chars 01Ol
	private final static char[] DEF_ALPHABET = ("23456789" + "ABCDEFGHIJKLMNPQRSTUVWXYZ"
			+ "abcdefghijkmnopqrstuvwxyz" + "#$!:.=+-/_").toCharArray();
	private final static SecureRandom r = new SecureRandom();

	public static String genPassword(final int len) {
		final char[] sb = new char[len];
		while (true) {
			boolean nums = false, mayus = false, minus = false, sign = false;
			for (int i = 0; i < len;) {
				final int j = ((r.nextInt() & 0x7FFFFFFF) % DEF_ALPHABET.length);
				final char c = DEF_ALPHABET[j];
				// No char repeat
				if ((i > 0) && (c == sb[i - 1])) {
					continue;
				}
				if ((c >= '0') && (c <= '9')) {
					nums = true;
				} else if ((c >= 'a') && (c <= 'z')) {
					minus = true;
				} else if ((c >= 'A') && (c <= 'Z')) {
					mayus = true;
				} else {
					// Don't begin/end with sign
					if ((i == 0) || (i == len - 1)) {
						continue;
					}
					sign = true;
				}
				sb[i++] = c;
			}
			// At least one character of each type
			if (nums && mayus && minus && sign)
				return (new String(sb));
		}
	}

	public static void main(final String[] args) {
		for (int i = 8; i <= 64; i *= 2) {
			System.out.println(i + "\t" + genPassword(i));
		}
		for (int i = 10; i <= 40; i *= 2) {
			System.out.println(i + "\t" + genPassword(i));
		}
	}
}
