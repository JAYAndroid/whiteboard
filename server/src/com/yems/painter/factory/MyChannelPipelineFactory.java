package com.yems.painter.factory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import com.yems.painter.handler.MyServerHandler;

/**
 * @ClassName: MyChannelPipelineFactory
 * @Description: 自定义管道线工厂类，用于管理通道里的各个handler
 * @author lwtx-yems
 * @date Mar 6, 2015 10:45:55 AM
 * 
 */
public class MyChannelPipelineFactory implements ChannelPipelineFactory {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();

		pipeline.addLast("objectencoder", new ObjectEncoder());

		pipeline.addLast("objectdecoder", new ObjectDecoder(ClassResolvers
				.cacheDisabled(this.getClass().getClassLoader())));
		pipeline.addLast("serverhandler", new MyServerHandler());
		return pipeline;
	}

}
