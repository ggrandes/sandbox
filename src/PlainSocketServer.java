import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple TCP Server Socket that listen and print to STDOUT
 */
public class PlainSocketServer {
	//
	private static final boolean QUIET = true;
	private static final int HTTP_TCP = 7777;
	private static final ExecutorService threadPool = Executors.newCachedThreadPool();

	public static void main(String[] args) throws Exception {
		ServerSocket listen = new ServerSocket();
		listen.setReuseAddress(true);
		listen.bind(new InetSocketAddress(HTTP_TCP));
		System.out.println("Listen in: " + listen);
		while (true) {
			final Socket sock = listen.accept();
			sock.setSendBufferSize(0xFFFF);
			sock.setReceiveBufferSize(0xFFFF);
			System.out.println("New connection from: " + sock);
			threadPool.submit(new Runnable() {
				public void run() {
					BufferedReader in = null;
					try {
						in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
						// Read Request
						while (true) {
							final String line = in.readLine();
							if (line == null || line.isEmpty())
								break;
							System.out.println("Data: " + line);
						}
					} catch (IOException e) {
						if (!QUIET)
							System.out.println(e.toString() + " " + sock);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					try { in.close(); } catch (Exception ign) { }
					try { sock.close(); } catch (Exception ign) { }
				}
			});
		}
	}
}
