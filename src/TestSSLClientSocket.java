import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class TestSSLClientSocket {

	public static SSLParameters setupSSLParams(SSLContext ctx) {
		List<String> protos = Arrays.asList(new String[] { 
				"TLSv1", 
				"SSLv3" 
		});
		List<String> suites = Arrays.asList(new String[] {
				"TLS_RSA_WITH_AES_256_CBC_SHA",
				"TLS_RSA_WITH_AES_128_CBC_SHA",
				"SSL_RSA_WITH_3DES_EDE_CBC_SHA",
				"SSL_RSA_WITH_RC4_128_SHA"
		});
		SSLParameters sslParams = ctx.getSupportedSSLParameters();
		protos.retainAll(Arrays.asList(sslParams.getProtocols()));
		suites.retainAll(Arrays.asList(sslParams.getCipherSuites()));
		sslParams.setProtocols(protos.toArray(new String[0]));
		sslParams.setCipherSuites(suites.toArray(new String[0]));
		return sslParams;
	}

	public static void main(String[] args) throws Exception {
		char[] pwd = "changeit".toCharArray();
		// Import X.509 Certificate
		File crtFile = new File(System.getProperty("java.io.tmpdir"), "test.crt");
		FileInputStream isCrt = new FileInputStream(crtFile);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate crt = (X509Certificate)cf.generateCertificate(isCrt);
		isCrt.close();
		//
		// Init SSL Factory
		KeyStore identity = KeyStore.getInstance(KeyStore.getDefaultType());
		identity.load(null);
		identity.setCertificateEntry(crt.getSubjectX500Principal().getName(), crt);
		SSLContext ctx = SSLContext.getInstance("TLS");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(identity, pwd);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(identity);
		ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		SSLSocketFactory factory = ctx.getSocketFactory();
		//
		// Setup SSL Parameters
		SSLParameters sslParams = setupSSLParams(ctx);
		//
		// Create Socket
		SSLSocket sock = (SSLSocket) factory.createSocket();
		sock.setEnabledCipherSuites(sslParams.getCipherSuites());
		sock.setEnabledProtocols(sslParams.getProtocols());
		// Display Settings
		System.out.println("Protos: " + Arrays.asList(sock.getEnabledProtocols()));
		System.out.println("Suites: " + Arrays.asList(sock.getEnabledCipherSuites()));
		//
		// Connect
		sock.connect(new InetSocketAddress("127.0.0.1", 1991), 30000);
		sock.startHandshake();
		System.out.println("Connect: " + sock);
		System.out.println("SSL Session: " + sock.getSession());
		OutputStream out = sock.getOutputStream();
		try {
			while (true) {
				final String send = "Test! " + System.currentTimeMillis();
				System.out.println(send);
				out.write(send.getBytes());
				out.write('\n');
				try { Thread.sleep(1000); } catch (Exception ign) {}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
