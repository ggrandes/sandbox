import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Client for EchoServer that measures latency
 */
public class EchoClient {
	//
	private static final boolean QUIET = true;
	private static final int ECHO_TCP = 7;
	private static final char[] chars = "0123456789abcdefefghijklmnopqrstuvwxyzABCDEFEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray();

	//
	public static void main(String[] args) throws Exception {
		final Socket sock = new Socket(args[0], ECHO_TCP);
		System.out.println("Connected to: " + sock);
		sock.setSendBufferSize(0xFFFF);
		sock.setReceiveBufferSize(0xFFFF);
		OutputStream os = null;
		InputStream is = null;
		try {
			os = sock.getOutputStream();
			is = sock.getInputStream();
			final byte[] buf = new byte[1];
			int c = 0;
			long min = Long.MAX_VALUE, max = Long.MIN_VALUE, avg = 0;
			os.write(' ');
			is.read(buf);
			while (c < 10000) {
				final long ts = System.nanoTime();
				os.write(chars[++c % chars.length]);
				final int n = is.read(buf);
				if (n < 0)
					break;
				final long t = System.nanoTime() - ts;
				if (t < min) {
					min = t;
				} else if (t > max) {
					max = t;
				}
				avg += t;
			}
			System.out.println("min=" + min / 1000f + "\u00B5s\tmax=" + max / 1000f + "\u00B5s\tavg="
					+ (avg / c / 1000f) + "\u00B5s\tcount=" + c);
		} catch (IOException e) {
			if (!QUIET)
				System.out.println(e.toString() + " " + sock);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			is.close();
		} catch (Exception ign) {
		}
		try {
			os.close();
		} catch (Exception ign) {
		}
		try {
			sock.close();
		} catch (Exception ign) {
		}
	}
}
