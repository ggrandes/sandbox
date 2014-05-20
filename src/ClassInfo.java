import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassInfo {
	/**
	 * 0: Throwable, 1: getCurrentMethod, 2: Invoker
	 */
	private static final int MIN_DEPTH = 2;

	/**
	 * Get Method name for current class
	 * 
	 * @param invokingClass
	 * @param prefixWithClass
	 * @param unknownName
	 * @return invokingMethod
	 */
	public static final String getCurrentMethod(final Class<?> invokingClass, //
			final boolean prefixWithClass, final String unknownName) {
		final String searchClassName = invokingClass.getName();
		final TraceHelper traceHelper = new TraceHelper();
		final int len = traceHelper.getStackTraceDepth();
		for (int i = MIN_DEPTH; i < len; i++) {
			final StackTraceElement ste = traceHelper.getStackTraceElement(i);
			final String className = ste.getClassName();
			if (className.equals(searchClassName)) {
				final String method = ste.getMethodName();
				return (prefixWithClass ? className + "#" + method : //
						method);
			}
		}
		return unknownName;
	}

	/**
	 * Get Method name for current class
	 * 
	 * @param invokingClass
	 * @return invokingMethod
	 */
	public static final String getCurrentMethod(final Class<?> invokingClass) {
		return getCurrentMethod(invokingClass, true, "");
	}

	public static class TraceHelper {
		private static final Logger log = Logger.getLogger(TraceHelper.class.getName());
		private static Method m1;
		private static Method m2;
		private Throwable t;
		private StackTraceElement[] ste;

		static {
			try {
				m1 = Throwable.class.getDeclaredMethod("getStackTraceDepth");
				m1.setAccessible(true);
				m2 = Throwable.class.getDeclaredMethod("getStackTraceElement", int.class);
				m2.setAccessible(true);
			} catch (Exception e) {
				m1 = null;
				m2 = null;
				log.log(Level.WARNING, "Unable to setAccesible(getStackTraceXXX): " + //
						e.toString(), e);
			}
		}

		public TraceHelper() {
			t = new Throwable();
			if ((m1 == null) || (m2 == null)) {
				ste = t.getStackTrace();
			}
		}

		public int getStackTraceDepth() {
			if (m1 != null) {
				try {
					return ((Integer) m1.invoke(t)).intValue();
				} catch (Exception e) {
					m1 = null;
					log.log(Level.WARNING, "Unable to invoke(getStackTraceDepth): " + //
							e.toString(), e);
				}
			}
			return ste.length;
		}

		public StackTraceElement getStackTraceElement(final int depth) {
			if (m2 != null) {
				try {
					return (StackTraceElement) m2.invoke(t, depth);
				} catch (Exception e) {
					m2 = null;
					log.log(Level.WARNING, "Unable to invoke(getStackTraceElement): " + //
							e.toString(), e);
				}
			}
			return ste[depth];
		}
	}
}
