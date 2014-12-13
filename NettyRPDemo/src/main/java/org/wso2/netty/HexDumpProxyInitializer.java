package org.wso2.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class HexDumpProxyInitializer extends ChannelInitializer<SocketChannel> {
	private final String remoteHost;
	private final int remotePort;

	private final boolean isSecuredProxy;
	private final boolean isSecureBackend;

	private final String keyStoreLocation;
	private final String keyStorePassword;

	private final String trustStoreLocation;
	private final String trustStorePassword;

	public HexDumpProxyInitializer(String remoteHost, int remotePort, boolean isSecuredProxy,
	                               String keyStoreLocation, String keyStorePassword,
	                               boolean isSecuredBackend, String trustStoreLocation,
	                               String trustStorePasswprd) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.isSecuredProxy = isSecuredProxy;
		this.keyStoreLocation = keyStoreLocation;
		this.keyStorePassword = keyStorePassword;
		this.isSecureBackend = isSecuredBackend;
		this.trustStoreLocation = trustStoreLocation;
		this.trustStorePassword = trustStorePasswprd;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		if (isSecuredProxy) {
			/*
			 * Sets a secured channel between the client and the reverse proxy
			 * service. This is a Server SSL context since proxy is acting as a
			 * server for this connection..
			 */
			// pipeline.addLast(sslContext.newHandler(ch.alloc()));

			SSLContext serverSSLContext =
			                              SSLUtil.createServerSSLContext(keyStoreLocation,
			                                                             keyStorePassword);
			SSLEngine sslEngine = serverSSLContext.createSSLEngine();
			sslEngine.setUseClientMode(false);
			pipeline.addLast("ssl", new SslHandler(sslEngine));
		}

		pipeline.addLast(new LoggingHandler(LogLevel.INFO),
		                 new HexDumpProxyFrontendHandler(remoteHost, remotePort, isSecureBackend,
		                                                 trustStoreLocation, trustStorePassword));

	}
}
