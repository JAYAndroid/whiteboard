package com.yems.painter.factory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import com.yems.painter.control.PainterCanvasControl;
import com.yems.painter.handler.MyClientHandler;
import com.yems.painter.listener.ConnectStateListener;

/**
 * @description: 自定义管道线工厂类，用于管理通道里的各个handler
 * @date: 2015-3-16 下午1:57:49
 * @author: yems
 */
public class MyChannelPipelineFactory implements ChannelPipelineFactory
{
	/** 处理网络数据业务的客户端对象 */
	private MyClientHandler myClientHandler;

	public MyChannelPipelineFactory()
	{
		myClientHandler = new MyClientHandler();
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception
	{
		ChannelPipeline pipeline = Channels.pipeline();
		// 对象编码器
		pipeline.addLast("objectencoder", new ObjectEncoder());
		// 对象解码器
		pipeline.addLast("objectdecoder", new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
		// 网络数据业务处理
		pipeline.addLast("clientHandler", myClientHandler);
		return pipeline;
	}

	/**
	 * @param painterCanvas
	 * @description: 设置画布对象
	 * @date: 2015-3-16 下午2:00:22
	 * @author: yems
	 */
	public void setPainterCanvas(PainterCanvasControl painterCanvas)
	{
		myClientHandler.setPainterCanvas(painterCanvas);
	}

	public void setConnectListener(ConnectStateListener onnectStateListener)
	{
		myClientHandler.setConnectListener(onnectStateListener);
	}
}
