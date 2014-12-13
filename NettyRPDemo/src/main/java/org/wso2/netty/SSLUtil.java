package org.wso2.netty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SSLUtil {
	private static String KEY_STORE_TYPE = "JKS";
	private static String TRUST_STORE_TYPE = "JKS";
	private static String KEY_MANAGER_TYPE = "SunX509";
	private static String TRUST_MANAGER_TYPE = "SunX509";
	private static String PROTOCOL = "TLS";

	private static SSLContext serverSSLCtx = null;
	private static SSLContext clientSSLCtx = null;

	public static SSLContext createServerSSLContext(final String keyStoreLocation,
	                                                final String keyStorePwd) {
		try {
			if (serverSSLCtx == null) {
				KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
				keyStore.load(new FileInputStream(keyStoreLocation), keyStorePwd.toCharArray());
				KeyManagerFactory keyManagerFactory =
				                                      KeyManagerFactory.getInstance(KEY_MANAGER_TYPE);
				keyManagerFactory.init(keyStore, keyStorePwd.toCharArray());
				serverSSLCtx = SSLContext.getInstance(PROTOCOL);
				serverSSLCtx.init(keyManagerFactory.getKeyManagers(), null, null);
			}
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return serverSSLCtx;
	}

	public static SSLContext createClientSSLContext(final String trustStoreLocation,
	                                                final String trustStorePwd) {
		try {
			if (clientSSLCtx == null) {
				KeyStore trustStore = KeyStore.getInstance(TRUST_STORE_TYPE);
				trustStore.load(new FileInputStream(trustStoreLocation),
				                trustStorePwd.toCharArray());
				TrustManagerFactory trustManagerFactory =
				                                          TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
				trustManagerFactory.init(trustStore);
				clientSSLCtx = SSLContext.getInstance(PROTOCOL);
				clientSSLCtx.init(null, trustManagerFactory.getTrustManagers(), null);
			}
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return clientSSLCtx;

	}

}
