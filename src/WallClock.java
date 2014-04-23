/**
 * This class implements a WallClock, like System.currentTimeMillis(), with less overhead (but less precise).
 * <p>
 * Sample times over 50.000.000 iterations:
 * <ul>
 * <li>SystemClock: 1057ms</li>
 * <li>WallClock: 190ms</li>
 * </ul>
 * 
 * @see System#currentTimeMillis()
 */
public class WallClock implements Runnable {
	private volatile int clockLatency = 1;           // WallClock time refresh (millis)
	private volatile long lastTimeMillis = 0L;       // Last time cached from SystemClock
	private volatile boolean runningThread = false;  // Thread running?
	private volatile Thread clocker = null;          // WallClock Thread

	// Singleton
	private static WallClock self = null;

	private WallClock() {
	}

	/**
	 * Get instance of WallClock
	 * 
	 * @return
	 */
	public static synchronized WallClock getInstance() {
		if (self == null) {
			self = new WallClock();
			self.start();
		}
		return self;
	}

	/**
	 * Set new wall clock refresh time (millis)
	 * 
	 * @param clockLatency
	 */
	public void setClockLatency(final int clockLatency) {
		this.clockLatency = clockLatency;
	}

	/**
	 * Return WallClock time (System.currentTimeMillis() compatible)
	 * 
	 * @return
	 */
	public long currentTimeMillis() {
		if (runningThread && (lastTimeMillis > 0)) {
			return lastTimeMillis;
		}
		throw new IllegalStateException("WallClock not started");
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
	 * Run, baby, run
	 */
	public void run() {
		try {
			lastTimeMillis = System.currentTimeMillis();
			runningThread = true;
			synchronized (clocker) {
				clocker.notifyAll();
			}
			while (!Thread.currentThread().isInterrupted()) {
				lastTimeMillis = System.currentTimeMillis();
				Thread.sleep(clockLatency);
			}
		} catch (InterruptedException ie) {
			/* Allow thread to exit */
		} finally {
			runningThread = false;
			clocker = null;
			lastTimeMillis = 0;
		}
	}

	/**
	 * Start wallclock if not already started
	 */
	private void start() {
		if ((clocker == null) || (!clocker.isAlive())) {
			clocker = new Thread(this);
			clocker.setDaemon(true);
			clocker.setName("WallClock-" + System.currentTimeMillis());
			try {
				synchronized (clocker) {
					clocker.start();
					final int waitCount = (3000 / clockLatency); // Wait 3 seconds
					for (int i = 0; i < waitCount; i++) {
						clocker.wait(clockLatency);
						if (runningThread)
							break;
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Destroy WallClock
	 */
	public void destroy() {
		if ((!runningThread) || (clocker == null)) {
			throw new IllegalStateException("WallClock already stoped");
		}
		clocker.interrupt();
		final int waitCount = (3000 / clockLatency); // Wait 3 seconds
		for (int i = 0; i < waitCount; i++) {
			try {
				Thread.sleep(clockLatency);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			if (!runningThread) {
				break;
			}
		}
		if (runningThread) {
			throw new RuntimeException("WallClock Stop FAILED");
		}
	}

	/**
	 * Simple Benchmark
	 * 
	 * @throws Throwable
	 */
	public static void main(final String[] args) throws Throwable {
		final int TOTAL = 50000000;
		long ax = 0, begin = 0;
		//
		begin = System.currentTimeMillis();
		for (int i = 0; i < TOTAL; i++) {
			ax += (System.currentTimeMillis() & 0xFF);
		}
		System.out.println("SystemClock: " + (System.currentTimeMillis() - begin) + "ms" + " ax=" + ax);
		System.out.println("SystemClock T1: " + System.currentTimeMillis());
		Thread.sleep(2);
		System.out.println("SystemClock T2: " + System.currentTimeMillis());
		//
		WallClock wc = WallClock.getInstance();
		begin = System.currentTimeMillis();
		for (int i = 0; i < TOTAL; i++) {
			ax += (wc.currentTimeMillis() & 0xFF);
		}
		System.out.println("WallClock: " + (System.currentTimeMillis() - begin) + "ms" + " ax=" + ax);
		System.out.println("WallClock T1: " + wc.currentTimeMillis());
		Thread.sleep(2);
		System.out.println("WallClock T2: " + wc.currentTimeMillis());
		wc.destroy();
	}
}