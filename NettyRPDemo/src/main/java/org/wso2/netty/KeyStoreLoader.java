package org.wso2.netty;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;

public class KeyStoreLoader {

	private static final String PROTOCOL = "TLS";
	// private static final SSLContext SERVER_CONTEXT;
	private static final SSLContext CLIENT_CONTEXT;

	static {
		String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
		if (algorithm == null) {
			algorithm = "SunX509";
		}
		KeyManagerFactory kmf = null;
		SSLContext serverContext;
		SSLContext clientContext;
		try {
			KeyStore ks = KeyStore.getInstance("JKS");

			FileInputStream fin = new FileInputStream(HexDumpProxy.TRUST_STORE_LOCATION);
			ks.load(fin, HexDumpProxy.TRUST_STORE_PASSWORD.toCharArray());

			// Set up key manager factory to use our key store
			kmf = KeyManagerFactory.getInstance(algorithm);
			kmf.init(ks, HexDumpProxy.TRUST_STORE_PASSWORD.toCharArray());

			// Initialize the SSLContext to work with our key managers.
			// serverContext = SSLContext.getInstance(PROTOCOL);
			// serverContext.init(kmf.getKeyManagers(), null, null);
		} catch (Exception e) {
			throw new Error("Failed to initialize the server-side SSLContext", e);
		}

		try {
			clientContext = SSLContext.getInstance(PROTOCOL);
			clientContext.init(kmf.getKeyManagers(),
			                   SecureChatTrustManagerFactory.getTrustManagers(), null);
		} catch (Exception e) {
			throw new Error("Failed to initialize the client-side SSLContext", e);
		}

		// SERVER_CONTEXT = serverContext;
		CLIENT_CONTEXT = clientContext;
	}

	// public static SSLContext getServerContext() {
	// return SERVER_CONTEXT;
	// }

	public static SSLContext getClientContext() {
		return CLIENT_CONTEXT;
	}

	// private SecureChatSslContextFactory() {
	// // Unused
	// }
}
