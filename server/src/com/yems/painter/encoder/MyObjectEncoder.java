package com.yems.painter.encoder;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import com.yems.painter.serializable.SerializablePath;

public class MyObjectEncoder extends ObjectEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		if (msg instanceof SerializablePath) {
			return msg;
		}
		return super.encode(ctx, channel, msg);
	}
}
