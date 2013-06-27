import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

/**
 * Generate random text [0-9a-zA-Z], width 79 columns, over HTTP Inspired in
 * http://en.wikipedia.org/wiki/Character_Generator_Protocol
 */
public class HttpRandomClient {
	//
	private static final boolean QUIET = true;
	private static final int HTTP_TCP = 9994;
	private static final int LINE_WIDTH = 79;
	private static final int GEN_LINES = 1000;
	private static final char[] chars = "0123456789abcdefefghijklmnopqrstuvwxyzABCDEFEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray();
	private static final String END_HEADER = "end";
	private static final String BODY = "FAKE-BODY";
	

	public static void main(String[] args) throws Exception {
		final Socket sock = new Socket("localhost", HTTP_TCP);
		sock.setSendBufferSize(0xFFFF);
		sock.setReceiveBufferSize(0xFFFF);
		System.out.println("New connection from: " + sock);
		Random rand = new Random();
		PrintWriter out = null;
		int count = 0;
		try {
			out = new PrintWriter(sock.getOutputStream());
			// Send Request
			final int lengthEOL = System.getProperty("line.separator").length();
			final int contentLength = (BODY.length() + lengthEOL);
			out.println("PUT / HTTP/1.0");
			out.println("Content-Type: text/plain; charset=US-ASCII");
			out.println("Connection: close");
			out.println("Content-Length: " + contentLength);
			out.println("Cache-Control: private, no-store, no-cache");
			out.println("Pragma: no-cache");
			int lines = 0;
			out.print("Header" + (100000 + lines) + ": ");
			while (lines < GEN_LINES) {
				final int n = (rand.nextInt() & 0x7FFFFFFF); // Erase bit sign
				out.write(chars[n % chars.length]);
				count++;
				if ((count % LINE_WIDTH) == 0) {
					out.println();
					out.flush();
					++lines;
					out.print("Header" + (100000 + lines) + ": ");
				}
			}
			out.println(END_HEADER);
			out.println();
			out.flush();
			out.println(BODY);
			out.flush();
		} catch (IOException e) {
			if (!QUIET)
				System.out.println(e.toString() + " " + sock);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try { out.close(); } catch (Exception ign) { }
		try { sock.close(); } catch (Exception ign) { }
	}
}
