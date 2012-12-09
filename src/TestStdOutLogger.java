import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestStdOutLogger {

	public static void main(String[] args) {
		System.setOut(new PrintStream(new AutoRotateFileOutputStream("/tmp/log.out", "yyyy-MM-dd.HHmmss")));
		System.setErr(new PrintStream(new AutoRotateFileOutputStream("/tmp/log.err")));
		long ts = System.currentTimeMillis();
		for (int i = 0; i < (int)5; i++) {
			System.out.println("test: " + System.currentTimeMillis());
			try { Thread.sleep(500); } catch (Exception ign) {}
		}
		System.err.println("Time=" + (System.currentTimeMillis() - ts));
	}

	static class AutoRotateFileOutputStream extends OutputStream {
		final String filename;
		final SimpleDateFormat sdf;
		final LinkedHashMap<Integer, String> cache = new LinkedHashMap<Integer, String>() {
			private static final long serialVersionUID = 1L;
			protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
				return (size() > 10);
			}
		};
		String currentStamp = null;
		FileOutputStream os = null;

		/**
		 * Creates a file output stream with default daily pattern (yyyy-MM-dd) rotation
		 * @param filename
		 */
		public AutoRotateFileOutputStream(final String filename) {
			this(filename, "yyyy-MM-dd");
		}

		/**
		 * Creates a file output stream with specified pattern rotation
		 * @param filename
		 * @param pattern like SimpleDateFormat: yyyy-MM-dd.HHmmss
		 */
		public AutoRotateFileOutputStream(final String filename, final String pattern) {
			this.filename = filename;
			this.sdf = new SimpleDateFormat(pattern);
		}

		private final String getTimeStamp() {
			final Integer now = Integer.valueOf((int) (System.currentTimeMillis() / 1000));
			String nowString = cache.get(now);
			if (nowString == null) {
				nowString = sdf.format(new Date(now.longValue()*1000));
				cache.put(now, nowString);
			}
			return nowString;
		}

		private final void open() throws IOException {
			final String newStamp = getTimeStamp();
			if (newStamp != currentStamp) {
				close();
			}
			if (os == null) {
				final String out = filename + "." + newStamp;
				os = new FileOutputStream(out, true);
				currentStamp = newStamp;
			}
		}

		@Override
		public synchronized void close() throws IOException {
			if (os != null) {
				os.flush();
				os.close();
				os = null;
			}
		}

		@Override
		public synchronized void flush() throws IOException {
			if (os != null)
				os.flush();
		}

		@Override
		public synchronized void write(byte[] b) throws IOException {
			if ((os != null) && ((b[0] == '\n') || (b[0] == '\r'))) {
				os.write(b);
			} else {
				open();
				os.write(b);
			}
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			if ((os != null) && ((b[off] == '\n') || (b[off] == '\r'))) {
				os.write(b, off, len);
			} else {
				open();
				os.write(b, off, len);
			}
		}

		@Override
		public synchronized void write(int b) throws IOException {
			if ((os != null) && ((b == '\n') || (b == '\r'))) {
				os.write(b);
			} else {
				open();
				os.write(b);
			}
		}
	}
}
