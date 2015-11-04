package com.yems.painter.control;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import android.util.Log;

import com.yems.painter.common.Commons;
import com.yems.painter.entity.MetadataRepositories;
import com.yems.painter.factory.MyChannelPipelineFactory;
import com.yems.painter.factory.MyThreadFactory;
import com.yems.painter.listener.ConnectStateListener;
import com.yems.painter.serializable.SerializablePath;

/**
 * @description: 网络电子白板-客户端(单例模式),是 基于C/S模式的交互绘图板
 * @date: 2015-3-12 上午9:03:17
 * @author: yems
 */
public class ClientControl implements Serializable
{
	private String TAG = "ClientNetty";
	/** 自定义管道管理 */
	private MyChannelPipelineFactory myChannelPipelineFactory;
	private static ClientControl instance;
	private ConnectStateListener mConnectStateListener;

	public static ClientControl getInstance()
	{
		if (instance == null)
		{
			instance = new ClientControl();
		}
		return instance;
	}

	/**
	 * 
	 * @description: 释放客户端对象资源
	 * @date: 2015-3-20 下午12:37:23
	 * @author: yems
	 */
	public void releaseInstance()
	{
		if (instance != null)
		{
			instance = null;
		}
	}

	private ClientControl()
	{
		myChannelPipelineFactory = new MyChannelPipelineFactory();
	}

	/**
	 * @return void
	 * @description: 连接到服务端
	 * @date 2015-3-16 下午10:08:27
	 * @author: yems
	 */
	public void connect()
	{
		MyThreadFactory.getInstance().getExecutorService().submit(new Runnable()
		{

			@Override
			public void run()
			{
				// Client服务启动器
				ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
				// 设置一个处理服务端消息和各种消息事件的类(Handler)
				bootstrap.setPipelineFactory(myChannelPipelineFactory);
				// 连接到指定IP地址的服务端
				bootstrap.setOption("key", "demokey");
				Channel channel = bootstrap.connect(new InetSocketAddress(Commons.SERVER_IP_ADDRESS, Commons.SERVER_PORT)).awaitUninterruptibly().getChannel();

				if (channel.isOpen())
				{
					Log.i(TAG, "通道成功打开");
					sendMyUUID2Server();
				} else
				{
					Log.i(TAG, "通道打开失败");
				}
			}
		});

	}

	/**
	 * @return void
	 * @description: 发送当前设备唯一标示符给服务端
	 * @date 2015-3-16 下午10:08:44
	 * @author: yems
	 */
	private void sendMyUUID2Server()
	{
		SerializablePath path = new SerializablePath();
		path.setMyUUID(Commons.myUUID);
		path.setOPType("send_uuid");
		write(path);
	}

	/**
	 * @return void
	 * @description: 上传图形数据到服务端
	 * @date 2015-3-16 下午10:08:59
	 * @author: yems
	 */
	public void commitShapToServer()
	{
		List<SerializablePath> shapes = MetadataRepositories.getInstance().getBufferShapes();
		System.out.println("客户端：向服务器写入数据");
		if (shapes.size() != 0)
		{
			SerializablePath serializablePath = shapes.get(shapes.size() - 1);
			write(serializablePath);
			System.out.println("客户端：写入数据完毕！");
		}

	}

	/**
	 * @return void
	 * @description: 清除服务端所有的图形数据
	 * @date 2015-3-16 下午10:09:10
	 * @author: yems
	 */
	public void clearAllShapes()
	{
		SerializablePath path = new SerializablePath();
		path.setOPType("clear");
		path.setMyUUID(Commons.myUUID);
		write(path);
	}

	/**
	 * @return void
	 * @description: 向服务端发送数据
	 * @date 2015-3-16 下午10:09:20
	 * @author: yems
	 */
	private void write(Object object)
	{
		if (Commons.currentChannel != null && Commons.currentChannel.isOpen())
		{
			Log.i(TAG, "开始向服务端发送数据");
			Commons.currentChannel.write(object);
		}
	}

	/**
	 * @return void
	 * @description: 设置画布对象
	 * @date 2015-3-16 下午10:09:30
	 * @author: yems
	 */
	public void setPainterCanvas(PainterCanvasControl painterCanvas)
	{
		myChannelPipelineFactory.setPainterCanvas(painterCanvas);
	}

	public void setConnectListener(ConnectStateListener connectStateListener)
	{
		myChannelPipelineFactory.setConnectListener(connectStateListener);
	}
}
