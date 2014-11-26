package org.wso2.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

public class HexDumpProxyInitializer extends ChannelInitializer<SocketChannel> {
	private final String remoteHost;
	private final int remotePort;
	// private final SslContext sslContext;

	private final SslContext sslContext;

	public HexDumpProxyInitializer(String remoteHost, int remotePort, SslContext sslCtx) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.sslContext = sslCtx;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (sslContext != null) {
			/*
			 * Sets a secured channel between the client and the reverse proxy
			 * service. This is a Server SSL context since proxy is acting as a
			 * server for this connection..
			 */
			pipeline.addLast(sslContext.newHandler(ch.alloc()));

			/*
			 * TODO: This is another way of setting the SSL context. But you may
			 * ignore this for the moment.
			 */
			// SSLEngine sslEngine = sslContext.createSSLEngine();
			// sslEngine.setUseClientMode(false);
			// pipeline.addLast("ssl", new SslHandler(sslEngine));
			//
			// pipeline.addLast("decoder", new HttpRequestDecoder());
		}

		/*
		 * TODO: I tried adding SSL support between the reverse proxy and the
		 * backend service using this way also. But it also does NOT work. Here
		 * ESB acts as the SSL client. Please note the use of newClientContext
		 * here.
		 */
		// SslContext sslCtx = null;
		// try {
		// sslCtx =
		// SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
		// } catch (SSLException e) {
		// e.printStackTrace();
		// }
		// pipeline.addLast(new SslHandler(sslCtx.newEngine(ch.alloc(),
		// remoteHost, remotePort)));

		pipeline.addLast(new LoggingHandler(LogLevel.INFO),
		                 new HexDumpProxyFrontendHandler(remoteHost, remotePort));

	}
}
