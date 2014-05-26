import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agnostic Cache for Method Invokation using Reflection
 */
public class Memoizer implements InvocationHandler {
	private final Object object;
	private final HashMap<Method, ConcurrentHashMap<List<Object>, Object>> caches;

	/**
	 * Memoize object
	 * 
	 * @param object source
	 * @return proxied object
	 */
	public static Object memoize(final Object object) //
			throws InstantiationException, IllegalAccessException {
		final Class<?> clazz = object.getClass();
		final Memoizer memoizer = new Memoizer(object);
		return Proxy.newProxyInstance(clazz.getClassLoader(), //
				clazz.getInterfaces(), memoizer);
	}

	private Memoizer(final Object object) {
		this.object = object;
		this.caches = new HashMap<Method, ConcurrentHashMap<List<Object>, Object>>();
	}

	private synchronized ConcurrentHashMap<List<Object>, Object> getCache(final Method m) {
		ConcurrentHashMap<List<Object>, Object> cache = caches.get(m);
		if (cache == null) {
			cache = new ConcurrentHashMap<List<Object>, Object>();
			caches.put(m, cache);
		}
		return cache;
	}

	public Object invoke(final Object proxy, final Method method, //
			final Object[] args) throws Throwable {
		if (method.getReturnType().equals(Void.TYPE)) {
			// Don't cache void methods
			return invoke(method, args);
		} else {
			final ConcurrentHashMap<List<Object>, Object> cache = getCache(method);
			final List<Object> key = Arrays.asList(args);
			Object value = cache.get(key);
			if ((value == null) && !cache.containsKey(key)) {
				value = invoke(method, args);
				cache.put(key, value);
			}
			return value;
		}
	}

	private Object invoke(final Method method, final Object[] args) //
			throws Throwable {
		try {
			return method.invoke(object, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
}