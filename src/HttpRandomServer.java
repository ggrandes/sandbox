import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generate random text [0-9a-zA-Z], width 79 columns, over HTTP Inspired in
 * http://en.wikipedia.org/wiki/Character_Generator_Protocol
 */
public class HttpRandomServer {
	//
	private static final boolean QUIET = true;
	private static final int HTTP_TCP = 8080;
	private static final int LINE_WIDTH = 79;
	private static final int GEN_LINES = 1000;
	private static final char[] chars = "0123456789abcdefefghijklmnopqrstuvwxyzABCDEFEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	private static final ExecutorService threadPool = Executors.newCachedThreadPool();
	private static final String FOOTER = "=== END ===";

	public static void main(String[] args) throws Exception {
		ServerSocket listen = new ServerSocket(HTTP_TCP);
		System.out.println("Listen in: " + listen);
		while (true) {
			final Socket sock = listen.accept();
			sock.setSendBufferSize(0xFFFF);
			sock.setReceiveBufferSize(0xFFFF);
			System.out.println("New connection from: " + sock);
			threadPool.submit(new Runnable() {
				public void run() {
					Random rand = new Random();
					BufferedReader in = null;
					PrintWriter out = null;
					int count = 0;
					try {
						in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
						out = new PrintWriter(sock.getOutputStream());
						// Read Request
						while (true) {
							final String line = in.readLine();
							if (line == null || line.isEmpty())
								break;
							System.out.println("Request: " + line);
						}
						// Send Response
						final int lengthEOL = System.getProperty("line.separator").length();
						final int lengthBODY = (GEN_LINES * (LINE_WIDTH + lengthEOL));
						final int contentLength = (lengthBODY + FOOTER.length() + lengthEOL);
						out.println("HTTP/1.0 200 OK");
						out.println("Content-Type: text/plain; charset=US-ASCII");
						out.println("Connection: close");
						out.println("Content-Length: " + contentLength);
						out.println("Cache-Control: private, no-store, no-cache");
						out.println("Pragma: no-cache");
						out.println();
						int lines = 0;
						while (lines < GEN_LINES) {
							final int n = (rand.nextInt() & 0x7FFFFFFF); // Erase bit sign
							out.write(chars[n % chars.length]);
							count++;
							if ((count % LINE_WIDTH) == 0) {
								out.println();
								out.flush();
								++lines;
							}
						}
						out.println(FOOTER);
						out.flush();
					} catch (IOException e) {
						if (!QUIET)
							System.out.println(e.toString() + " " + sock);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					try { in.close(); } catch (Exception ign) { }
					try { out.close(); } catch (Exception ign) { }
					try { sock.close(); } catch (Exception ign) { }
				}
			});
		}
	}
}
