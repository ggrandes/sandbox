import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

// Alternative to String.intern()
public class StringPoolWH {
	private static final WeakHashMap<String,WeakReference<String>> map = new WeakHashMap<String,WeakReference<String>>();

	public static synchronized String getCanonicalVersion(final String str) {
		final WeakReference<String> ref = map.get(str);
		if (ref != null) {
			final String cstr = (String) ref.get();
			if (cstr != null) {
				return cstr;
			}
		}
		map.put(str, new WeakReference<String>(str));
		return str;
	}
}
