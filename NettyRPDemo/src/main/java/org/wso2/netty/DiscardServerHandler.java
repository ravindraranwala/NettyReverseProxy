package org.wso2.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class DiscardServerHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
		try {
			ByteBuf in = (ByteBuf) msg;
			System.out.println(in.toString(io.netty.util.CharsetUtil.US_ASCII));
		} finally {
			ReferenceCountUtil.release(msg); // (2)
		}
		// Release occurs implicitly when the data is written to the wire.
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}

}
