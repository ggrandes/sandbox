import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * The ThrottleStream provides bandwidth throttling on a specified InputStream/OutputStream.
 * It is implemented as a wrapper for another InputStream/OutputStream instance.
 */
public class ThrottleStream {
	protected final int speedLimitBytesSecond;
	protected int currentTransferredBytes = 0;
	protected long lastTimeStampSeconds = 0;

	public static ThrottleInputStream createThrottleInputStream(final InputStream in,
			final int maxSpeedInKilobytesPerSecond) {
		return new ThrottleInputStream(in, maxSpeedInKilobytesPerSecond);
	}

	public static ThrottleOutputStream createThrottleOutputStream(final OutputStream out,
			final int maxSpeedInKilobytesPerSecond) {
		return new ThrottleOutputStream(out, maxSpeedInKilobytesPerSecond);
	}

	protected ThrottleStream(final int maxSpeedInKilobytesPerSecond) {
		this.speedLimitBytesSecond = maxSpeedInKilobytesPerSecond * 1024;
	}

	protected void addAndWait(final int transferredBytes) throws IOException {
		long now = System.currentTimeMillis() / 1000;
		if (now != lastTimeStampSeconds) {
			currentTransferredBytes = 0;
			lastTimeStampSeconds = now;
		}
		currentTransferredBytes += transferredBytes;
		if (currentTransferredBytes >= speedLimitBytesSecond) {
			try {
				while (true) {
					Thread.sleep(10);
					now = System.currentTimeMillis() / 1000;
					if (now != lastTimeStampSeconds) {
						currentTransferredBytes = 0;
						lastTimeStampSeconds = now;
						break;
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException(e);
			}
		}
	}

	public static class ThrottleInputStream extends FilterInputStream {
		final ThrottleStream limit;

		public ThrottleInputStream(final InputStream in, final int maxSpeedInKilobytesPerSecond) {
			super(in);
			limit = new ThrottleStream(maxSpeedInKilobytesPerSecond);
		}

		@Override
		public int read() throws IOException {
			final int read = super.read();
			limit.addAndWait(read);
			return read;
		}

		@Override
		public int read(final byte[] b) throws IOException {
			final int read = super.read(b);
			limit.addAndWait(read);
			return read;
		}

		@Override
		public int read(final byte[] b, final int off, final int len) throws IOException {
			final int read = super.read(b, off, len);
			limit.addAndWait(read);
			return read;
		}
	}

	public static class ThrottleOutputStream extends FilterOutputStream {
		final ThrottleStream limit;

		public ThrottleOutputStream(final OutputStream out, final int maxSpeedInKilobytesPerSecond) {
			super(out);
			limit = new ThrottleStream(maxSpeedInKilobytesPerSecond);
		}

		@Override
		public void write(final int b) throws IOException {
			super.write(b);
			limit.addAndWait(1);
		}

		@Override
		public void write(final byte[] b) throws IOException {
			super.write(b);
			limit.addAndWait(b.length);
		}

		@Override
		public void write(final byte[] b, final int off, final int len) throws IOException {
			super.write(b, off, len);
			limit.addAndWait(len);
		}
	}

	/**
	 * Simple Test
	 * 
	 * @param args
	 */
	public static void main(final String[] args) throws Throwable {
		PrintStream out = new PrintStream(ThrottleStream.createThrottleOutputStream(System.out, 1)); // 1KB/sec
		for (int i = 0; i < 1000000; i++) {
			out.println("Hello world in Throttle Mode, count=" + i);
		}
	}
}
