import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic Singleton with support for WeakReferences
 */
public class GenericSingleton<T> {
	private static final GenericSingleton<Object> DEFAULT_HARD = createInstance();
	private static final GenericSingleton<Object> DEFAULT_WEAK = createWeakInstance();
	private final Map<Object, Object> map;

	/**
	 * Get Default GenericSingleton instance (HashMap)
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> GenericSingleton<T> getDefaultInstance() {
		return (GenericSingleton<T>) DEFAULT_HARD;
	}

	/**
	 * Get Default GenericSingleton instance (WeakHashMap)
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> GenericSingleton<T> getDefaultWeakInstance() {
		return (GenericSingleton<T>) DEFAULT_WEAK;
	}

	/**
	 * Create GenericSingleton instance (hard references)
	 * 
	 * @return
	 */
	public static <T> GenericSingleton<T> createInstance() {
		return new GenericSingleton<T>(false);
	}

	/**
	 * Create GenericSingleton instance (weak references)
	 * 
	 * @return
	 */
	public static <T> GenericSingleton<T> createWeakInstance() {
		return new GenericSingleton<T>(true);
	}

	/**
	 * Create GenericSingleton
	 * 
	 * @param weak true for WeakHashMap or false for HashMap
	 */
	private GenericSingleton(final boolean weak) {
		this.map = (weak ? new WeakHashMap<Object, Object>() : new HashMap<Object, Object>());
	}

	/**
	 * Get Instance if exist, and if not, use initCallback for creation
	 * 
	 * @param id
	 * @param initCallback
	 * @return
	 */
	private synchronized T get0(final Object id, final GenericSingletonInitCallback<T> initCallback) {
		@SuppressWarnings("unchecked")
		T instance = (T) map.get(id);
		if ((instance == null) && (initCallback != null)) {
			instance = initCallback.initialValue();
			map.put(id, instance);
		}
		return instance;
	}

	/**
	 * Put Instance (overwrite)
	 * 
	 * @param id
	 * @param instance
	 */
	private synchronized void put0(final Object id, final T instance) {
		map.put(id, instance);
	}

	/**
	 * Remove Instance
	 * 
	 * @param id
	 */
	private synchronized void remove0(final Object id) {
		map.remove(id);
	}

	/**
	 * Get Instance if exist, and if not, use initCallback for creation
	 * 
	 * @param uuid
	 * @param initCallback
	 * @return
	 */
	public T get(final UUID uuid, final GenericSingletonInitCallback<T> initCallback) {
		return get0(uuid, initCallback);
	}

	/**
	 * Get Instance if exist, and if not, use initCallback for creation
	 * 
	 * @param name
	 * @param initCallback
	 * @return
	 */
	public T get(final String name, final GenericSingletonInitCallback<T> initCallback) {
		return get0(name, initCallback);
	}

	/**
	 * Get Instance if exist
	 * 
	 * @param uuid
	 * @return
	 */
	public T get(final UUID uuid) {
		return get0(uuid, null);
	}

	/**
	 * Get Instance if exist
	 * 
	 * @param name
	 * @return
	 */
	public T get(final String name) {
		return get0(name, null);
	}

	/**
	 * Put Instance (overwrite)
	 * 
	 * @param uuid
	 * @param instance
	 */
	public void put(final UUID uuid, final T instance) {
		put0(uuid, instance);
	}

	/**
	 * Put Instance (overwrite)
	 * 
	 * @param name
	 * @param instance
	 */
	public void put(final String name, final T instance) {
		put0(name, instance);
	}

	/**
	 * Remove Instance
	 * 
	 * @param uuid
	 */
	public void remove(final UUID uuid) {
		remove0(uuid);
	}

	/**
	 * Remove Instance
	 * 
	 * @param name
	 */
	public void remove(final String name) {
		remove0(name);
	}

	/**
	 * Callback Interface for lazy initialization in GenericSingleton
	 */
	public static interface GenericSingletonInitCallback<T> {
		public T initialValue();
	}

	/**
	 * Simple Test
	 */
	public static void main(final String[] args) throws Throwable {
		final String name = "com.acme.test.users.map";
		final GenericSingletonInitCallback<Map<String, String>> cb = new GenericSingletonInitCallback<Map<String, String>>() {
			@Override
			public Map<String, String> initialValue() {
				final Map<String, String> map = new ConcurrentHashMap<String, String>();
				return map;
			}
		};
		//
		GenericSingleton<Map<String, String>> gs = null;
		//
		gs = GenericSingleton.getDefaultInstance();
		gs.get(name, cb).put("user1", "1234");
		System.out.println(gs.get(name, cb).toString());
		gs = GenericSingleton.getDefaultInstance();
		gs.get(name, cb).put("user2", "5678");
		System.out.println(gs.get(name, cb).toString());
		//
		gs = GenericSingleton.createInstance();
		gs.get(name, cb).put("user3", "9012");
		System.out.println(gs.get(name, cb).toString());
		gs.get(name, cb).put("user4", "3456");
		System.out.println(gs.get(name, cb).toString());
	}
}
