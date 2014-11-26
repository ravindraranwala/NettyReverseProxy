package org.wso2.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HexDumpProxyBackendHandler extends ChannelInboundHandlerAdapter {
	private final Channel inboundChannel;

	public HexDumpProxyBackendHandler(Channel inboundChannel) {
		this.inboundChannel = inboundChannel;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.read();
		ctx.write(Unpooled.EMPTY_BUFFER);
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) {
		// Sends the response from the backend service to the client.
		inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (future.isSuccess()) {
					ctx.channel().read();
				} else {
					future.channel().close();
				}
			}
		});
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		HexDumpProxyFrontendHandler.closeOnFlush(inboundChannel);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		HexDumpProxyFrontendHandler.closeOnFlush(ctx.channel());
	}

}
