import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

/**
 * URL connection check that Follow Redirects and get SSL Info
 */
public class TestFollowURL {
	private static final String URL_TEST = "http://github.com";
	private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
	private static final int DEFAULT_READ_TIMEOUT = 60000;
	private static final boolean DEFAULT_FOLLOW_REDIRECTS = false;
	private static final int DEFAULT_MAX_FOLLOW_REDIRECTS = 10;
	private static final int DEFAULT_MAX_BODY_LINES = 5;
	private static final int DEFAULT_MAX_LINE_LENGTH = 76;
	private static final int DEFAULT_MAX_CERTIFICATES = 3;

	public static void main(final String[] args) {
		for (final String line : checkURL(URL_TEST)) {
			System.out.println(line);
		}
	}

	public static List<String> checkURL(final String urlCheck) {
		HttpURLConnection conn = null;
		InputStream is = null;
		ArrayList<String> response = new ArrayList<String>();
		ArrayList<String> urlList = new ArrayList<String>();
		urlList.add(urlCheck);
		//
		for (int j = 0; j < urlList.size() && j < DEFAULT_MAX_FOLLOW_REDIRECTS; j++) {
			final String u = urlList.get(j);
			try {
				final URL url = new URL(u);
				conn = (HttpURLConnection) url.openConnection();
				conn.setInstanceFollowRedirects(DEFAULT_FOLLOW_REDIRECTS);
				conn.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
				conn.setReadTimeout(DEFAULT_READ_TIMEOUT);
				conn.setDoOutput(false);
				conn.setDoInput(true);
				conn.setRequestProperty("User-Agent", "TestFollowURL");
				conn.setRequestProperty("Accept", "text/*");
				conn.setRequestProperty("Connection", "close"); // Disable keepAlive
				conn.connect();
				if (conn instanceof HttpsURLConnection) {
					final HttpsURLConnection sslConn = (HttpsURLConnection) conn;
					response.add("[CipherSuite: " + String.valueOf(sslConn.getCipherSuite()) + "]");
					int c = 0;
					for (final Certificate cert : sslConn.getServerCertificates()) {
						final String info = getCertificateInfo(cert);
						if (info != null) {
							response.add("[CertificateInfo: " + info + "]");
						}
						if (++c >= DEFAULT_MAX_CERTIFICATES) {
							break;
						}
					}
				}
				// Get the response
				final Set<Entry<String, List<String>>> resHeaders = conn.getHeaderFields().entrySet();
				final Iterator<Entry<String, List<String>>> i = resHeaders.iterator();
				while (i.hasNext()) {
					final Entry<String, List<String>> e = i.next();
					final String headerName = e.getKey();
					if (headerName == null) { // HTTP/1.X YYY ETC
						response.add(String.valueOf(conn.getHeaderField(0)));
						continue;
					}
					for (final String headerValue : e.getValue()) {
						response.add(headerName + ": " + headerValue);
					}
				}
				response.add("");
				// Follow Redirect
				final String location = conn.getHeaderField("Location");
				if (location != null) {
					urlList.add(location);
					continue;
				}
				// Content-Length or Transfer-Encoding indicate body in request
				final int responseBodyLength = conn.getContentLength();
				final boolean transferEncoding = (conn.getHeaderField("Transfer-Encoding") != null);
				final boolean doInput = ((responseBodyLength > 0) || transferEncoding);
				//
				if (doInput) {
					try {
						is = conn.getInputStream();
					} catch (Exception e) {
						is = conn.getErrorStream();
					}
					if (is != null) {
						final String contentType = conn.getHeaderField("Content-Type");
						if (!String.valueOf(contentType).startsWith("text/")) {
							response.add("[...cut...]");
							continue;
						}
						final BufferedReader in = new BufferedReader(new InputStreamReader(is));
						try {
							String line = null;
							int c = 0;
							while ((line = in.readLine()) != null) {
								if (line.length() > DEFAULT_MAX_LINE_LENGTH) {
									response.add(line.substring(0, DEFAULT_MAX_LINE_LENGTH) + "[...cut...]");
								} else {
									response.add(line);
								}
								if (++c >= DEFAULT_MAX_BODY_LINES) {
									response.add("[...cut...]");
									break;
								}
							}
						} finally {
							closeQuietly(in);
						}
					}
				}
			} catch (Exception e) {
				response.add(String.valueOf(e));
				for (final StackTraceElement t : e.getStackTrace()) {
					response.add("\tat " + String.valueOf(t));
				}
			} finally {
				closeQuietly(is);
			}
		}
		return response;
	}

	private static String getCertificateInfo(final Certificate cert) {
		if (cert instanceof X509Certificate) {
			final X509Certificate x509 = (X509Certificate) cert;
			return "{ " + x509.getPublicKey().getAlgorithm() + ", " + //
					x509.getSigAlgName() + ", " + //
					toStringDate(x509.getNotAfter()) + " }" + //
					"{ " + x509.getSubjectDN().getName() + " }";
		}
		return null;
	}

	private static String toStringDate(final Date d) {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(d);
	}

	private static void closeQuietly(final InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (Exception ign) {
			}
		}
	}

	private static void closeQuietly(final Reader in) {
		if (in != null) {
			try {
				in.close();
			} catch (Exception ign) {
			}
		}
	}
}
