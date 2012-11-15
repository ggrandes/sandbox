import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * Normas para los Passwords:
 * <ul> 
 * <li>Al menos 8 characters y no más de 28 caracteres</li>
 * <li>Al menos 1 caracter de cada grupo:</li>
 * <ul>
 * <li>Letras mayusculas <b>[A-Z]</b></li>
 * <li>Letras minusculas <b>[a-z]</b></li>
 * <li>Numeros <b>[0-9]</b></li>
 * <li>Caracteres especiales: <b><[{(#$%&*?!:.,=+-_~^)}]></b></li>
 * </ul>
 * <li>No espacios</li>
 * <li>No letras acentuadas</li>
 * <li>No partes del username o ID</li>
 * </ul>
 *
 */
public class PasswordChecker {
	static final char[] special_chars = "<[{(#$%&*?!:.,=+-_~^)}]>".toCharArray();
	//
	boolean dirty = true;
	//
	int countMay = 0; // Mayusculas
	int countMin = 0; // Minusculas
	int countNum = 0; // Numeros
	int countEsp = 0; // Especiales
	int countInv = 0; // Invalidos
	//
	final Pattern pnum = Pattern.compile("(\\d+)"); 	// numbers
	final Pattern plet = Pattern.compile("([a-z]+)"); 	// letters
	boolean invalidWords = true;
	//
	String user;
	String password;
	//

	// Static init
	{
		Arrays.sort(special_chars);
	}

	public PasswordChecker() {
		this("", "");
	}
	public PasswordChecker(String user, String password) {
		this.user = user;
		this.password = password;
		setDirty(true);
	}
	final void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}

	public final PasswordChecker setUser(final String user) {
		this.user = user;
		setDirty(true);
		return this;
	}

	public final PasswordChecker setPassword(final String password) {
		this.password = password;
		setDirty(true);
		return this;
	}

	final void checkWords() {
		if (checkPattern(plet) || checkPattern(pnum)) {
			invalidWords = true;
		}
		else {
			invalidWords = false;
		}
	}

	final boolean checkPattern(final Pattern pat) {
		final String chkPassword = password.toLowerCase();
		final Matcher m = pat.matcher(user.toLowerCase());
		while (m.find()) {
			final String word = m.group();
			if (word.length() < 3) continue; // Skip short words
			if (chkPassword.contains(word)) {
				return true;
			}
		}
		return false;
	}

	final void countChars() {
		final int len = password.length();
		// Reset Counters
		countMay = countMin = countNum = countEsp = countInv = 0; 
		for (int i = 0; i < len; i++) {
			final char c = password.charAt(i);
			if ((c >= 'A') && (c <= 'Z')) {
				countMay++;
			}
			else if ((c >= 'a') && (c <= 'z')) {
				countMin++;
			}
			else if ((c >= '0') && (c <= '9')) {
				countNum++;
			}
			else if (Arrays.binarySearch(special_chars, c) >= 0) {
				countEsp++;
			}
			else {
				countInv++;
			}
		}
	}

	public boolean isValid() {
		if (dirty) {
			countChars();
			checkWords();
			setDirty(false);
		}
		return (
				(password.length() >= 8) && (password.length() <= 28) && 
				(countMay > 0) && (countMin > 0) && 
				(countNum > 0) && (countEsp > 0) &&
				(countInv == 0) && 
				(!invalidWords)
				);
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (dirty) {
			countChars();
			checkWords();
			setDirty(false);
		}
		sb
		.append("Length=").append(password.length()).append(" ")
		.append("Mayusculas=").append(countMay).append(" ")
		.append("Minusculas=").append(countMin).append(" ")
		.append("Numeros=").append(countNum).append(" ")
		.append("Especiales=").append(countEsp).append(" ")
		.append("Invalidos=").append(countInv).append(" ")
		.append("BlackWords=").append(invalidWords);
		return sb.toString();
	}

	static void testCheck(final String user, final String pass) {
		final StringBuilder sb = new StringBuilder(); 
		final PasswordChecker pwdchk = new PasswordChecker();
		pwdchk.setUser(user);
		pwdchk.setPassword(pass);
		sb
		.append("pwd=").append(pass).append(" ")
		.append("valid=").append(pwdchk.isValid()).append(" ")
		.append(pwdchk.toString());
		System.out.println(sb.toString());
	}

	public static void main(final String[] args) {
		testCheck("", "abCd9?"); // invalid
		testCheck("", "abcdefghik9?"); // invalid
		testCheck("", "abcdefghikABCDEFGHIJK-?*94231"); // invalid
		testCheck("", "azbZfg1?"); // valid
		testCheck("Usuario12", "UsuAriO?12"); // valid > invalid!
		testCheck("Z694812", "Zz!694812"); // valid > invalid!
		testCheck("SuperMan", "4r76*(SHhMi3"); // valid
	}
}
