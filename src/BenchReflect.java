import java.lang.reflect.Method;

/**
 * Example of output:
 * Loop iteration counter=5000000
 * Normal Static time=78ms
 * Normal Object time=78ms
 * Reflection Static time=796ms
 * Reflection Object time=796ms
 * Static XX=96349939634993
 * Object XX=96349939634993
 */
public class BenchReflect {
	public static void main(String... args) throws Exception {
		final int TOTAL = (int) 5e6;
		final InvokedObject invokedObj = new InvokedObject();
		final Object dummyObj = new Object();
		long ts;

		log("Loop iteration counter=" + TOTAL);
		InvokedStatic.doStatic(dummyObj);
		invokedObj.doObject(dummyObj);
		System.gc();

		// Benchmark Normal Invocation (Static)
		if (true) {
			ts = System.currentTimeMillis();
			for (int i = 0; i < TOTAL; i++) {
				InvokedStatic.doStatic(dummyObj);
			}
			log("Normal Static time=", ts);
		}

		// Benchmark Normal Invocation (Object)
		if (true) { 
			ts = System.currentTimeMillis();
			for (int i = 0; i < TOTAL; i++) {
				invokedObj.doObject(dummyObj);
			}
			log("Normal Object time=", ts);
		}

		// Benchmark Reflection Invocation (Static)
		if (true) {
			final Class<InvokedStatic> c = InvokedStatic.class;
			final Method ref = c.getMethod("doStatic", Object.class);
			ts = System.currentTimeMillis();
			for (int i = 0; i < TOTAL; i++) {
				ref.invoke(null, dummyObj);
			}
			log("Reflection Static time=", ts);
		}

		// Benchmark Reflection Invocation (Object)
		if (true) {
			final Class<InvokedObject> c = InvokedObject.class;
			final Method ref = c.getMethod("doObject", Object.class);
			ts = System.currentTimeMillis();
			for (int i = 0; i < TOTAL; i++) {
				ref.invoke(invokedObj, dummyObj);
			}
			log("Reflection Object time=", ts);
		}

		log("Static XX=" + InvokedStatic.getXX());
		log("Object XX=" + invokedObj.getXX());
	}

	private static final void log(final String msg, final long ts) {
		System.out.println(msg + (System.currentTimeMillis() - ts) + "ms");
	}
	private static final void log(final String msg) {
		System.out.println(msg);
	}

	public static class InvokedStatic {
		private static long xx = 0;
		public static void doStatic(final Object dummy) {
			xx += dummy.hashCode();
		}
		public static long getXX() {
			return xx;
		}
	}

	public static class InvokedObject {
		private long xx = 0;
		public void doObject(final Object dummy) {
			xx += dummy.hashCode();
		}
		public long getXX() {
			return xx;
		}
	}
}
