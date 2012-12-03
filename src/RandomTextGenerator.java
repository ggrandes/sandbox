import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generate random text [0-9a-zA-Z], width 79 columns, over Socket
 * Inspired in http://en.wikipedia.org/wiki/Character_Generator_Protocol 
 */
public class RandomTextGenerator {
	//
	private static final boolean QUIET = true;
	private static final int CHARGEN_TCP = 19;
	private static final char[] chars = "0123456789abcdefefghijklmnopqrstuvwxyzABCDEFEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	private static final ExecutorService threadPool = Executors.newCachedThreadPool();
	//
	public static void main(String[] args) throws Exception {
		ServerSocket listen = new ServerSocket(CHARGEN_TCP);
		System.out.println("Listen in: " + listen);
		while (true) {
			final Socket sock = listen.accept();
			sock.setSendBufferSize(0xFFFF);
			sock.setReceiveBufferSize(0xFFFF);
			System.out.println("New connection from: " + sock);
			threadPool.submit(new Runnable() {
				public void run() {
					Random rand = new Random();
					OutputStream os = null;
					int count = 0;
					try {
						os = new BufferedOutputStream(sock.getOutputStream(), 80);
						while (true) {
							final int n = (rand.nextInt() & 0x7FFFFFFF); // Erase bit sign
							os.write(chars[n % chars.length]);
							count++;
							if ((count % 79) == 0) {
								os.write(10);
								os.flush();
							}
						}
					} catch (IOException e) {
						if (!QUIET) System.out.println(e.toString() + " " + sock);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					try { os.close(); } catch(Exception ign) {}
					try { sock.close(); } catch(Exception ign) {}
				}
			});
		}
	}
}
