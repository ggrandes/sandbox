import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Show Security Providers, Algorithms and SSL Cipher Suites
 */
public class SecurityProviders {
	public static void main(final String[] args) throws Throwable {
		for (final Provider provider : Security.getProviders()) {
			System.out.println("Provider: " + provider.getName());
			for (final Provider.Service service : provider.getServices()) {
				System.out.println("  Algorithm: " + service.getAlgorithm());
			}
		}
		System.out.println("SSL Suites:");
		final SSLContext sslContext = SSLContext.getDefault();
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		final Set<String> defaultSuites = new HashSet<String>();
		defaultSuites.addAll(Arrays.asList(sslSocketFactory.getDefaultCipherSuites()));
		for (final String suite : sslSocketFactory.getSupportedCipherSuites()) {
			final boolean isDefault = defaultSuites.contains(suite);
			System.out.println("  Suite: " + suite + (isDefault ? " (DEFAULT)" : ""));
		}
	}
}
