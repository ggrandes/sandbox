import java.security.MessageDigest;
import java.util.Random;

/**
 * Benchmark MessageDigest Algorithms
 * 
 * @link <a href="http://technobcn.wordpress.com/2013/09/25/java-benchmark-messagedigest-algorithms/">Java:
 *       Benchmark MessageDigest Algorithms</a>
 */
public class BenchMarkMessageDigest {
	static final String[] TEST_ALGORITHMS = { 
		"MD5", "SHA-1", "SHA-256", "SHA-384", "SHA-512"
	};
	static final int LOOPS = 5;

	public static void main(final String[] args) throws Throwable {
		final Random r = new Random();
		// Seed random buffer
		final byte[] buf = new byte[32 * 1024 * 1024]; // 32MB
		final int BLOCK = 4096;
		for (int i = 0; i < buf.length; i += 4) {
			final int n = r.nextInt();
			buf[i + 0] = (byte) (n & 0xFF);
			buf[i + 1] = (byte) ((n >> 8) & 0xFF);
			buf[i + 2] = (byte) ((n >> 16) & 0xFF);
			buf[i + 3] = (byte) ((n >> 24) & 0xFF);
		}
		// Run Benchmarks
		MessageDigest md;
		long begin, diff;
		for (int t = 0; t < LOOPS; t++) {
			System.out.println("--- Loop: " + t);
			for (final String alg : TEST_ALGORITHMS) {
				begin = System.currentTimeMillis();
				md = MessageDigest.getInstance(alg);
				for (int i = 0; i < buf.length; i += BLOCK) {
					md.update(buf, i, BLOCK);
				}
				diff = (System.currentTimeMillis() - begin);
				System.out.println(alg + " time: " + diff + "ms");
			}
		}
	}
}
