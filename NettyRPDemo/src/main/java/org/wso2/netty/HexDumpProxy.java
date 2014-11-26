package org.wso2.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

public class HexDumpProxy {
	// static int LOCAL_PORT = Integer.parseInt(System.getProperty("localPort",
	// "8443"));
	// static String REMOTE_HOST = System.getProperty("remoteHost",
	// "w8cert.iconnectdata.com");
	// static final int REMOTE_PORT =
	// Integer.parseInt(System.getProperty("remotePort", "443"));

	static int LOCAL_PORT;
	static String REMOTE_HOST = null;
	static int REMOTE_PORT;
	private static Properties prop = new Properties();
	private static boolean isSsl = false;

	private static String KEY_STORE_TYPE = "JKS";
	private static String TRUST_STORE_TYPE = "JKS";
	private static String KEY_MANAGER_TYPE = "SunX509";
	private static String TRUST_MANAGER_TYPE = "SunX509";
	private static String PROTOCOL = "SSLv3";

	private static Logger logger = Logger.getLogger(HexDumpProxy.class.getName());

	public static void main(String[] args) throws InterruptedException {
		init();

		System.err.println("Proxying *:" + LOCAL_PORT + " to " + REMOTE_HOST + ':' + REMOTE_PORT +
		                   " ...");

		// Configure SSL.
		// SslContext sslContext = null;

		// SSLContext sslContext = null;
		SslContext sslCtx = null;
		if (isSsl) {
			// connection between the Client and reverse proxy is secured.
			try {
				SelfSignedCertificate ssc = new SelfSignedCertificate();
				sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
			} catch (CertificateException e) {
				e.printStackTrace();
			} catch (SSLException e) {
				e.printStackTrace();
			}
			// sslContext = createSSLContext();
			// try {
			// SelfSignedCertificate ssc = new
			// SelfSignedCertificate("/home/ravindra/.keystore");
			//
			// sslContext = SslContext.newServerContext(ssc.certificate(),
			// ssc.privateKey());
			// } catch (CertificateException ex) {
			// ex.printStackTrace();
			// } catch (SSLException ex) {
			// ex.printStackTrace();
			// }
		}

		// Configure the bootstrap.
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
			 .option(ChannelOption.SO_KEEPALIVE, true).handler(new LoggingHandler(LogLevel.INFO))
			 .childHandler(new HexDumpProxyInitializer(REMOTE_HOST, REMOTE_PORT, sslCtx))
			 .childOption(ChannelOption.AUTO_READ, false).bind(LOCAL_PORT).sync().channel()
			 .closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}

	/**
	 * This method is NOT used for the moment. This can be used to create an SSL
	 * context with the server keystore and the client trust store.
	 * 
	 * @return SSL context with the keystore and the client trust store.
	 */
	public static SSLContext createSSLContext() {
		logger.info("Creating the SSL context.");
		// SSLContext sslContext = null;
		// FileInputStream keyStoreIStream = null;
		// FileInputStream trustStoreIStream = null;
		SSLContext sslContext = null;
		try {
			final String clientTrustStorePath =
			                                    "/home/ravindra/ESB-Team/Support/Issues/WESTCORPDEV-153/esb/wso2esb-4.8.1/repository/resources/security/client-truststore.jks";
			final String keyStorePath =
			                            "/home/ravindra/ESB-Team/Support/Issues/WESTCORPDEV-153/esb/wso2esb-4.8.1/repository/resources/security/wso2carbon.jks";
			final String password = "wso2carbon";

			KeyStore keyStore = loadKeyStore(keyStorePath, password);
			KeyStore clientTrustStore = loadTrustStore(clientTrustStorePath, password);

			sslContext = initMutualSSLConnection(keyStore, clientTrustStore, password);
			// sslContext = SSLContext.getInstance("SSLv3");
			// final KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
			// keyStoreIStream =
			// new FileInputStream(
			// new File(
			// "/home/ravindra/ESB-Team/Support/Issues/WESTCORPDEV-153/esb/wso2esb-4.8.1/repository/resources/security/client-truststore.jks"));
			//
			// ks.load(keyStoreIStream, "wso2carbon".toCharArray());
			//
			// // Trust own CA and all self-signed certs
			// final KeyManagerFactory kmf =
			// KeyManagerFactory.getInstance("SunX509");
			//
			// kmf.init(ks, "wso2carbon".toCharArray());
			//
			// sslContext.init(kmf.getKeyManagers(), null, null);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("was thrown while creating the SSL context.");
		}

		return sslContext;
	}

	/**
	 * create basic SSL connection factory
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws KeyManagementException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 */
	public static SSLContext initMutualSSLConnection(final KeyStore keyStore,
	                                                 final KeyStore trustStore,
	                                                 final String keyStorePassword)
	                                                                               throws NoSuchAlgorithmException,
	                                                                               KeyStoreException,
	                                                                               KeyManagementException,
	                                                                               IOException,
	                                                                               UnrecoverableKeyException {
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KEY_MANAGER_TYPE);
		keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
		TrustManagerFactory trustManagerFactory =
		                                          TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
		trustManagerFactory.init(trustStore);
		SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),
		                null);

		return sslContext;
	}

	public static KeyStore loadKeyStore(String keyStorePath, String keyStorePassoword)
	                                                                                  throws KeyStoreException,
	                                                                                  IOException,
	                                                                                  CertificateException,
	                                                                                  NoSuchAlgorithmException {
		final KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(keyStorePath);
			keyStore.load(inputStream, keyStorePassoword.toCharArray());
		} finally {
			inputStream.close();
		}

		return keyStore;
	}

	/**
	 * load trust store with given .jks file
	 * 
	 * @param trustStorePath
	 * @param trustStorePassoword
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyStore loadTrustStore(String trustStorePath, String trustStorePassoword)
	                                                                                        throws KeyStoreException,
	                                                                                        IOException,
	                                                                                        CertificateException,
	                                                                                        NoSuchAlgorithmException {
		final KeyStore trustStore = KeyStore.getInstance(TRUST_STORE_TYPE);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(trustStorePath);
			trustStore.load(inputStream, trustStorePassoword.toCharArray());
		} finally {
			inputStream.close();
		}

		return trustStore;
	}

	/**
	 * reads the properties and starts the execution environment.
	 */
	private static void init() {
		InputStream input = null;

		try {

			input = new FileInputStream("src/main/resources/config.properties");

			// load a properties file
			prop.load(input);

			isSsl = Boolean.parseBoolean(prop.getProperty("ssl"));

			LOCAL_PORT = Integer.parseInt(prop.getProperty("localPort"));

			REMOTE_HOST = String.valueOf(prop.getProperty("remoteHost"));

			REMOTE_PORT = Integer.parseInt(prop.getProperty("remotePort"));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
