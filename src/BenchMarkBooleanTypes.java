import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple benchmark about: 
 * "AtomicBoolean" vs "volatile boolean" vs "boolean" vs "synchronized boolean"
 * <a href="http://technobcn.wordpress.com/2013/01/13/java-atomic-volatile-synchronized/">Java: Atomic, volatile, synchronized?</a>
 */
public class BenchMarkBooleanTypes {
	private static boolean b = true;
	private static volatile boolean vb = true;
	private static final AtomicBoolean ab = new AtomicBoolean(true);
	//
	private static final void setAB(final boolean pb) {
		ab.set(pb);
	}
	private static final void setVB(final boolean pb) {
		vb = pb;
	}
	private static final void setB(final boolean pb) {
		b = pb;
	}
	private static final synchronized void setBsync(final boolean pb) {
		b = pb;
	}
	//
	public static void main(String[] args) {
		final int TOTAL = (int) 1e8;
		long ts, diff;
		// Do anything
		System.out.println("B\t" + b);
		System.out.println("VB\t" + vb);
		System.out.println("AB\t" + ab.get());
		//
		// boolean
		ts = System.currentTimeMillis();
		for (int i = 0; i < TOTAL; i++) {
			setB(((i % 2) == 0) ? true : false);
		}
		diff = (System.currentTimeMillis() - ts);
		System.out.println("B\t" + TOTAL + "\t" + diff + "ms\t"
				+ (TOTAL / Math.max(diff / 1000, 1)) + "req/s\t"
				+ (TOTAL / Math.max(diff, 1)) + "req/ms");
		//
		// synchronized boolean 
		ts = System.currentTimeMillis();
		for (int i = 0; i < TOTAL; i++) {
			setBsync(((i % 2) == 0) ? true : false);
		}
		diff = (System.currentTimeMillis() - ts);
		System.out.println("Bsync\t" + TOTAL + "\t" + diff + "ms\t"
				+ (TOTAL / Math.max(diff / 1000, 1)) + "req/s\t"
				+ (TOTAL / Math.max(diff, 1)) + "req/ms");
		//
		// volatile boolean
		ts = System.currentTimeMillis();
		for (int i = 0; i < TOTAL; i++) {
			setVB(((i % 2) == 0) ? true : false);
		}
		diff = (System.currentTimeMillis() - ts);
		System.out.println("VB\t" + TOTAL + "\t" + diff + "ms\t"
				+ (TOTAL / Math.max(diff / 1000, 1)) + "req/s\t"
				+ (TOTAL / Math.max(diff, 1)) + "req/ms");
		//
		// AtomicBoolean
		ts = System.currentTimeMillis();
		for (int i = 0; i < TOTAL; i++) {
			setAB(((i % 2) == 0) ? true : false);
		}
		diff = (System.currentTimeMillis() - ts);
		System.out.println("AB\t" + TOTAL + "\t" + diff + "ms\t"
				+ (TOTAL / Math.max(diff / 1000, 1)) + "req/s\t"
				+ (TOTAL / Math.max(diff, 1)) + "req/ms");
		// Do anything
		System.out.println("B\t" + b);
		System.out.println("VB\t" + vb);
		System.out.println("AB\t" + ab.get());
	}
}
