package org.wso2.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

public class HexDumpProxyFrontendHandler extends ChannelInboundHandlerAdapter {
	private final String remoteHost;
	private final int remotePort;

	private volatile Channel outboundChannel;

	public HexDumpProxyFrontendHandler(String remoteHost, int remotePort) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		final Channel inboundChannel = ctx.channel();


		// Start the connection attempt.
		Bootstrap b = new Bootstrap();
		//b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass())
        b.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
		 .handler(new SecureChatClientInitializer(inboundChannel))
		 .option(ChannelOption.AUTO_READ, false);
		ChannelFuture f = b.connect(remoteHost, remotePort);
		outboundChannel = f.channel();






		/*
		 * TODO: Use this to enable SSLcommunication between the proxy and the
		 * backend. Caution: This does NOT work properly. I have commented these
		 * things since they were NOT working properly. You may uncomment it and
		 * try to resolve the issue. Backend is called just using HTTP. There's
		 * no SSL/TLS working for the moment. ESB acts as the SSL client. Please
		 * note the use of newClientContext here.
		 */

		// SslContext sslCtx = null;
		// try {
		// sslCtx =
		// SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
		// } catch (SSLException e) {
		// e.printStackTrace();
		// }
		// outboundChannel.pipeline().addLast(sslCtx.newHandler(outboundChannel.alloc(),
		// remoteHost,
		// remotePort));

		/*
		 * TODO: This is another alternative way of setting the SSL context
		 * using the trust
		 * store
		 * and keystore. Caution: This also does NOT work at the moment.
		 */

		// SSLEngine sslEngine =
		// HexDumpProxy.createSSLContext().createSSLEngine();
		//
		// sslEngine.setUseClientMode(true);
		// outboundChannel.pipeline().addLast(new SslHandler(sslEngine));

		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (future.isSuccess()) {
					// connection complete start to read first data
					inboundChannel.read();
				} else {
					// Close the connection if the connection attempt has
					// failed.
					inboundChannel.close();
				}
			}
		});
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) {
		if (outboundChannel.isActive()) {
			/*
			 * Sends the client request to the backend service.
			 */
			outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) {
					if (future.isSuccess()) {
						// was able to flush out data, start to read the next
						// chunk
						ctx.channel().read();
					} else {
						future.channel().close();
					}
				}
			});
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		if (outboundChannel != null) {
			closeOnFlush(outboundChannel);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		closeOnFlush(ctx.channel());
	}

	/**
	 * Closes the specified channel after all queued write requests are flushed.
	 */
	static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}

}
