import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/**
 * http://stackoverflow.com/questions/1788031/how-can-i-have-multiple-ssl-certificates-for-a-java-server/1788047#1788047
 * http://stackoverflow.com/questions/1793979/registering-multiple-keystores-in-jvm
 * http://stackoverflow.com/questions/5871279/java-ssl-and-cert-keystore
 * http://docs.oracle.com/javase/6/docs/technotes/guides/security/SunProviders.html
 * http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html
 */
public class TestSSLServerSocket {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		char[] pwd = "changeit".toCharArray();
		// Generate RSA Key
		KeyPairGenerator kgAsym = KeyPairGenerator.getInstance("RSA");
		kgAsym.initialize(1024); // TODO: RSA { 1024, 1536, 2048 }
		KeyPair kp = kgAsym.genKeyPair();
		// Generate X.509 Certificate
		X509Certificate crt = TestX509.generateCertificate("CN=Test1", kp, 365);
		File crtFile = new File(System.getProperty("java.io.tmpdir"), "test.crt");
		TestX509.writeCertificate(new FileOutputStream(crtFile), crt);
		//
		// Init SSL Factory
		KeyStore identity = KeyStore.getInstance(KeyStore.getDefaultType());
		identity.load(null);
		identity.setKeyEntry("private", kp.getPrivate(), pwd, new Certificate[] { crt });
		SSLContext ctx = SSLContext.getInstance("TLS");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(identity, pwd);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(identity);
		ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		SSLServerSocketFactory factory = ctx.getServerSocketFactory();
		//
		SSLServerSocket listen = (SSLServerSocket) factory.createServerSocket(1991);
		// Setup
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
		protos.retainAll(Arrays.asList(listen.getEnabledProtocols()));
		suites.retainAll(Arrays.asList(listen.getEnabledCipherSuites()));
		//
		listen.setEnableSessionCreation(true);
		listen.setEnabledProtocols(protos.toArray(new String[0]));
		listen.setEnabledCipherSuites(suites.toArray(new String[0]));
		// Display
		System.out.println("Suites: " + suites);
		System.out.println("Protos: " + protos);
		// Start
		System.out.println("Listen: " + listen);
		while (true) {
			SSLSocket socket = (SSLSocket) listen.accept();
			try {
				socket.startHandshake();
				System.out.println("New client: " + socket + " session: " + socket.getSession());
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null) {
					System.out.println(line);
				}
				System.out.println("End client: " + socket + " session: " + socket.getSession());
			}
			catch (Exception e) {
				e.printStackTrace();
				try { Thread.sleep(1000); } catch (Exception ign) {}
			}
			finally {
				try { socket.close(); } catch (Exception ign) {}
			}
		}
	}

}
