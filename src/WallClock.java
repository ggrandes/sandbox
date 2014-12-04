import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * This class implements a WallClock, like System.currentTimeMillis(), with less overhead (but less precise).
 * This version implements a lightweight auto-shutdown of wallclock-thread using a WeakReference.
 * <p>
 * Sample times over 500.000.000 iterations:
 * <ul>
 * <li>SystemClock: 9476ms</li>
 * <li>WallClock: 768ms</li>
 * </ul>
 * 
 * @see System#currentTimeMillis()
 */
public class WallClock {
	private static volatile int clockLatency = 1;    // WallClock time refresh (millis)
	private volatile long lastTimeMillis = 0L;       // Last time cached from SystemClock

	// Singleton
	private static WeakReference<WallClock> self = null;

	private WallClock() {
	}

	/**
	 * Get instance of WallClock
	 * 
	 * @return
	 */
	public static synchronized WallClock getInstance() {
		WallClock wc = (self == null ? null : self.get());
		if (wc == null) {
			if (self != null)
				self.enqueue();
			wc = new WallClock();
			final ReferenceQueue<WallClock> q = new ReferenceQueue<WallClock>();
			self = new WeakReference<WallClock>(wc, q);
			wc.start(q);
		}
		return wc;
	}

	/**
	 * Set new wall clock refresh time (millis)
	 * 
	 * @param newClockLatency
	 */
	public void setClockLatency(final int newClockLatency) {
		clockLatency = newClockLatency;
	}

	/**
	 * Return WallClock time (System.currentTimeMillis() compatible)
	 * 
	 * @return
	 */
	public long currentTimeMillis() {
		return lastTimeMillis;
	}

	/**
	 * Return WallClock time
	 * 
	 * @param useSystem (true to force refresh)
	 * @return
	 */
	public long currentTimeMillis(final boolean useSystem) {
		if (useSystem) {
			lastTimeMillis = System.currentTimeMillis();
			return lastTimeMillis;
		}
		return currentTimeMillis();
	}

	/**
	 * Start a new wallclock
	 */
	private void start(final ReferenceQueue<WallClock> q) {
		final Thread clocker = new Thread(new ClockRunner(q));
		clocker.setDaemon(true);
		clocker.setName("WallClock-" + currentTimeMillis(true));
		clocker.start();
	}

	/**
	 * Destroy WallClock
	 */
	public void destroy() {
		self.enqueue();
	}

	private final int updateAndGetLatency() {
		lastTimeMillis = System.currentTimeMillis();
		return clockLatency;
	}

	private static final class ClockRunner implements Runnable {
		final ReferenceQueue<WallClock> q;

		public ClockRunner(final ReferenceQueue<WallClock> q) {
			this.q = q;
		}

		@Override
		public void run() {
			System.out.println("Thread started: " + Thread.currentThread().getName());
			try {
				while ((q.remove(getInstance().updateAndGetLatency()) == null)
						&& !Thread.currentThread().isInterrupted()) {
					// System.out.println("Thread wait: " + System.currentTimeMillis());
				}
			} catch (InterruptedException ie) {
				/* Allow thread to exit */
			} finally {
				System.out.println("Thread ended: " + Thread.currentThread().getName());
			}
		}
	}

	/**
	 * Simple Benchmark
	 * 
	 * @throws Throwable
	 */
	public static void main(final String[] args) throws Throwable {
		final int TOTAL = 500000000;
		long ax = 0, ts = 0;
		//
		ts = System.currentTimeMillis();
		for (int i = 0; i < TOTAL; i++) {
			ax += (System.currentTimeMillis() & 0xF);
		}
		System.out.println("SystemClock: " + (System.currentTimeMillis() - ts) + "ms");
		System.out.println("SystemClock T1: " + System.currentTimeMillis());
		Thread.sleep(2);
		System.out.println("SystemClock T2: " + System.currentTimeMillis());
		//
		WallClock wc = WallClock.getInstance();
		ts = System.currentTimeMillis();
		for (int i = 0; i < TOTAL; i++) {
			ax += (wc.currentTimeMillis() & 0xF);
		}
		System.out.println("WallClock: " + (System.currentTimeMillis() - ts) + "ms");
		System.out.println("WallClock T1: " + wc.currentTimeMillis());
		Thread.sleep(2);
		System.out.println("WallClock T2: " + wc.currentTimeMillis());
		if ((ax > 0) || (ts > 0)) // Dummy (always true)
			wc.destroy();
	}
}