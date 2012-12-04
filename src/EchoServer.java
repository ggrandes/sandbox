import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The server sends back an identical copy of the data it received over Socket.
 * Inspired in http://en.wikipedia.org/wiki/Echo_Protocol 
 */
public class EchoServer {
	//
	private static final boolean QUIET = true;
	private static final int ECHO_TCP = 7;
	private static final ExecutorService threadPool = Executors.newCachedThreadPool();
	//
	public static void main(String[] args) throws Exception {
		ServerSocket listen = new ServerSocket(ECHO_TCP);
		System.out.println("Listen in: " + listen);
		while (true) {
			final Socket sock = listen.accept();
			sock.setSendBufferSize(0xFFFF);
			sock.setReceiveBufferSize(0xFFFF);
			System.out.println("New connection from: " + sock);
			threadPool.submit(new Runnable() {
				public void run() {
					InputStream is = null;
					OutputStream os = null;
					try {
						is = sock.getInputStream();
						os = sock.getOutputStream();
						final byte[] buf = new byte[4096];
						while (true) {
							final int n = is.read(buf);
							if (n < 0) break;
							os.write(buf, 0, n);
						}
					} catch (IOException e) {
						if (!QUIET) System.out.println(e.toString() + " " + sock);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					try { is.close(); } catch(Exception ign) {}
					try { os.close(); } catch(Exception ign) {}
					try { sock.close(); } catch(Exception ign) {}
				}
			});
		}
	}
}
