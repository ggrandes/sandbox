import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SafeSimpleDateFormat is a thread safe wrapper for SimpleDateFormat,
 * using this, you can user among different threads
 * 
 * @author ggrandes
 */
public class SafeSimpleDateFormat {
	public static SynchronizedSimpleDateFormat getSynchronizedSimpleDateFormat(final String format) {
		return new SynchronizedSimpleDateFormat(format);
	}

	public static ThreadLocalSimpleDateFormat getThreadLocalSimpleDateFormat(final String format) {
		return new ThreadLocalSimpleDateFormat(format);
	}

	public static class SynchronizedSimpleDateFormat {
		private final DateFormat df;

		public SynchronizedSimpleDateFormat(final String format) {
			this.df = new SimpleDateFormat(format);
		}

		public synchronized String format(final Date date) {
			return df.format(date);
		}

		public synchronized String format(final long ts) {
			return this.format(new Date(ts));
		}

		public synchronized String format() {
			return this.format(new Date(System.currentTimeMillis()));
		}

		public synchronized Date parse(String string) throws ParseException {
			return df.parse(string);
		}
	}

	public static class ThreadLocalSimpleDateFormat {
		private static final int MAX_CACHE = 128; // Limit maximum number of cached DateFormats by ThreadLocal
		private static final ThreadLocal<Map<String, DateFormat>> threadLocalFormatters = new ThreadLocal<Map<String, DateFormat>>() {
			@Override
			public HashMap<String, DateFormat> initialValue() {
				return new LinkedHashMap<String, DateFormat>() {
					private static final long serialVersionUID = 42L;

					protected boolean removeEldestEntry(final Map.Entry<String, DateFormat> eldest) {
						return (size() > MAX_CACHE);
					}
				};
			}
		};

		private final String format;

		public ThreadLocalSimpleDateFormat(final String format) {
			this.format = format;
		}

		public String format(final Date date) {
			return getDateFormat(format).format(date);
		}

		public String format(final long ts) {
			return this.format(new Date(ts));
		}

		public String format() {
			return this.format(new Date(System.currentTimeMillis()));
		}

		public Date parse(String string) throws ParseException {
			return getDateFormat(format).parse(string);
		}

		public void clean() {
			threadLocalFormatters.remove();
		}

		private DateFormat getDateFormat(final String format) {
			final Map<String, DateFormat> formattersMap = threadLocalFormatters.get();
			DateFormat formatter = formattersMap.get(format);
			if (formatter == null) {
				formatter = new SimpleDateFormat(format);
				formattersMap.put(format, formatter);
			}
			return formatter;
		}

	}

	/**
	 * Sample of check thread unsafe of SimpleDateFormat
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(final String[] args) throws InterruptedException {
		final SimpleDateFormat t = new SimpleDateFormat("dd-MM-yyyy");
		final String testdata[] = {
				"01-03-1999", "14-02-2001", "31-12-2007"
		};
		final Runnable r[] = new Runnable[testdata.length];
		final Thread th[] = new Thread[testdata.length];
		final long ts = System.currentTimeMillis();
		for (int i = 0; i < r.length; i++) {
			final int i2 = i;
			r[i] = new Runnable() {
				public void run() {
					try {
						for (int j = 0; j < 100000; j++) {
							String str = testdata[i2];
							String str2 = null;
							/* synchronized(t) */{
								Date d = t.parse(str);
								str2 = t.format(d);
							}
							if (!str.equals(str2)) {
								throw new RuntimeException("date conversion failed after " + j
										+ " iterations. Expected " + str + " but got " + str2);
							}
						}
					} catch (ParseException e) {
						throw new RuntimeException("parse failed");
					}
				}
			};
			th[i] = new Thread(r[i]);
			th[i].start();
		}
		for (int i = 0; i < r.length; i++) {
			th[i].join();
		}
		System.out.println("SimpleDateFormat end time=" + (System.currentTimeMillis() - ts));
	}
}
